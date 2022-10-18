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
                tx.nyVarseltittel(kodeRef, tittel)
                tx.nyVarselforklaring(kodeRef, forklaring)
                tx.nyVarselhandling(kodeRef, handling)
            }
        }
    }

    internal fun oppdaterVarsel(kode: String, tittel: String, forklaring: String?, handling: String?, avviklet: Boolean) {
        sessionOf(dataSource, returnGeneratedKey = true).use {
            it.transaction { tx ->
                val kodeRef = tx.finnVarselRef(kode) ?: throw VarselException.KodeFinnesIkke(kode)
                val oppdatertAvviklet = tx.oppdaterAvviklet(kode, avviklet)
                val oppdatertTittel = tx.oppdaterTittel(kodeRef, tittel)
                val oppdatertForklaring = tx.oppdaterForklaring(kodeRef, forklaring)
                val oppdatertHandling = tx.oppdaterHandling(kodeRef, handling)
                if (!oppdatertForklaring && !oppdatertAvviklet && !oppdatertTittel && !oppdatertHandling)
                    throw VarselException.IngenEndring(kode)
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
                    SELECT tittel, forklaring, handling, t.varselkode_ref FROM
                        (
                        SELECT tittel, varselkode_ref FROM varsel_tittel t
                        WHERE vk.id = t.varselkode_ref ORDER BY t.opprettet DESC LIMIT 1
                        ) t INNER JOIN
                        (
                        SELECT forklaring, varselkode_ref FROM varsel_forklaring f
                        WHERE vk.id = f.varselkode_ref ORDER BY f.opprettet DESC LIMIT 1
                        ) f ON f.varselkode_ref = vk.id INNER JOIN
                        (
                        SELECT handling, varselkode_ref FROM varsel_handling h
                        WHERE vk.id = h.varselkode_ref ORDER BY h.opprettet DESC LIMIT 1
                        ) h ON h.varselkode_ref = vk.id
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

    private fun TransactionalSession.sisteTittel(kodeRef: Long): String? {
        @Language("PostgreSQL")
        val query = "SELECT tittel FROM varsel_tittel WHERE varselkode_ref = ? ORDER BY opprettet DESC"
        return run(queryOf(query, kodeRef).map { it.stringOrNull(1) }.asSingle)
    }

    private fun TransactionalSession.sisteForklaring(kodeRef: Long): String? {
        @Language("PostgreSQL")
        val query = "SELECT forklaring FROM varsel_forklaring WHERE varselkode_ref = ? ORDER BY opprettet DESC"
        return run(queryOf(query, kodeRef).map { it.stringOrNull(1) }.asSingle)
    }

    private fun TransactionalSession.sisteHandling(kodeRef: Long): String? {
        @Language("PostgreSQL")
        val query = "SELECT handling FROM varsel_handling WHERE varselkode_ref = ? ORDER BY opprettet DESC"
        return run(queryOf(query, kodeRef).map { it.stringOrNull(1) }.asSingle)
    }

    private fun TransactionalSession.oppdaterTittel(kodeRef: Long, tittel: String): Boolean {
        if (sisteTittel(kodeRef) == tittel) return false
        return nyVarseltittel(kodeRef, tittel)
    }

    private fun TransactionalSession.oppdaterForklaring(kodeRef: Long, forklaring: String?): Boolean {
        if (sisteForklaring(kodeRef) == forklaring) return false
        return nyVarselforklaring(kodeRef, forklaring)
    }

    private fun TransactionalSession.oppdaterHandling(kodeRef: Long, handling: String?): Boolean {
        if (sisteHandling(kodeRef) == handling) return false
        return nyVarselhandling(kodeRef, handling)
    }

    private fun TransactionalSession.nyVarseltittel(kodeRef: Long, tittel: String): Boolean {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES (?, ?)"
        return run(queryOf(query, kodeRef, tittel).asUpdateAndReturnGeneratedKey) != null
    }

    private fun TransactionalSession.nyVarselforklaring(kodeRef: Long, forklaring: String?): Boolean {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES (?, ?)"
        return run(queryOf(query, kodeRef, forklaring).asUpdateAndReturnGeneratedKey) != null
    }

    private fun TransactionalSession.nyVarselhandling(kodeRef: Long, handling: String?): Boolean {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_handling(varselkode_ref, handling) VALUES (?, ?)"
        return run(queryOf(query, kodeRef, handling).asUpdateAndReturnGeneratedKey) != null
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