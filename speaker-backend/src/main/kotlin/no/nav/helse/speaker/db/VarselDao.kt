package no.nav.helse.speaker.db

import io.ktor.http.HttpStatusCode
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.Varsel
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class VarselDao(private val dataSource: DataSource) {

    internal fun nyttVarsel(kode: String, tittel: String, forklaring: String?, handling: String?, avviklet: Boolean) {
        sessionOf(dataSource, returnGeneratedKey = true).use {
            it.transaction { tx ->
                val kodeRef = tx.nyVarselkode(kode, avviklet) ?: throw VarselException.KodeEksisterer(kode)
                tx.nyDefinisjon(kodeRef, tittel, forklaring, handling)
            }
        }
    }

    internal fun oppdaterVarsel(kode: String, tittel: String, forklaring: String?, handling: String?, avviklet: Boolean) {
        sessionOf(dataSource, returnGeneratedKey = true).use {
            it.transaction { tx ->
                val kodeRef = tx.finnVarselRef(kode) ?: throw VarselException.KodeFinnesIkke(kode)
                val oppdatertAvviklet = tx.oppdaterAvviklet(kode, avviklet)
                val oppdatertDefinisjon = tx.nyDefinisjon(kodeRef, tittel, forklaring, handling)
                if (!oppdatertAvviklet && !oppdatertDefinisjon) throw VarselException.IngenEndring(kode)
            }
        }
    }

    internal fun finnVarsler(): List<Varsel> {
        return sessionOf(dataSource).use {
            it.transaction { tx ->
                @Language("PostgreSQL")
                val query = """
                SELECT vk.kode, tittel, forklaring, handling, vk.avviklet
                FROM varselkode vk INNER JOIN LATERAL (
                    SELECT tittel, forklaring, handling, varselkode_ref FROM varsel_definisjon vd ORDER BY id DESC LIMIT 1
                ) rad ON rad.varselkode_ref = vk.id
                """

                tx.run(queryOf(query).map { row ->
                    Varsel(
                        varselkode = row.string("kode"),
                        tittel = row.string("tittel"),
                        forklaring = row.stringOrNull("forklaring"),
                        handling = row.stringOrNull("handling"),
                        avviklet = row.boolean("avviklet")
                    ) }.asList)
            }
        }
    }

    private fun TransactionalSession.nyVarselkode(kode: String, avviklet: Boolean): Long? {
        @Language("PostgreSQL")
        val query = "INSERT INTO varselkode(kode, avviklet) VALUES (?, ?) ON CONFLICT(kode) DO NOTHING"
        return run(queryOf(query, kode, avviklet).asUpdateAndReturnGeneratedKey)
    }

    private fun TransactionalSession.oppdaterAvviklet(kode: String, avviklet: Boolean): Boolean {
        @Language("PostgreSQL")
        val query = "UPDATE varselkode SET endret = now(), avviklet = ? WHERE kode = ? AND avviklet != ?"
        return run(queryOf(query, avviklet, kode, avviklet).asUpdate) != 0
    }

    private fun TransactionalSession.finnVarselRef(kode: String): Long? {
        @Language("PostgreSQL")
        val query = "SELECT id FROM varselkode WHERE kode = ?"
        return run(queryOf(query, kode).map { it.long(1) }.asSingle)
    }

    private fun TransactionalSession.nyDefinisjon(kodeRef: Long, tittel: String, forklaring: String?, handling: String?): Boolean {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES (?, ?, ?, ?)"
        return run(queryOf(query, kodeRef, tittel, forklaring, handling).asUpdateAndReturnGeneratedKey) != null
    }
}

internal sealed class VarselException(message: String): Exception(message) {
    internal abstract val httpStatusCode: HttpStatusCode

    internal class KodeEksisterer(kode: String): VarselException("Varselkode $kode eksisterer allerede") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.Conflict
    }

    internal class KodeFinnesIkke(kode: String): VarselException("Varselkode $kode finnes ikke") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotFound
    }

    internal class IngenEndring(kode: String): VarselException("Varsel for varselkode $kode ikke endret, da payload er lik det som finnes i databasen") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotModified
    }
}