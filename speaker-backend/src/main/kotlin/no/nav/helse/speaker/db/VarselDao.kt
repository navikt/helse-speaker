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
                val oppdatertTittel = tx.nyVarseltittel(kodeRef, tittel)
                val oppdatertForklaring = tx.nyVarselforklaring(kodeRef, forklaring)
                val oppdatertHandling = tx.nyVarselhandling(kodeRef, handling)
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
                    SELECT vk.kode, vt.tittel, vf.forklaring, vh.handling, vk.avviklet
                    FROM varselkode vk
                             INNER JOIN LATERAL (
                        SELECT forklaring
                        FROM varsel_forklaring
                        WHERE vk.id = varsel_forklaring.varselkode_ref
                        ORDER BY varsel_forklaring.opprettet DESC
                        LIMIT 1
                        ) vf ON true
                             INNER JOIN LATERAL (
                        SELECT tittel
                        FROM varsel_tittel
                        WHERE vk.id = varsel_tittel.varselkode_ref
                        ORDER BY varsel_tittel.opprettet DESC
                        LIMIT 1
                        ) vt ON true
                            INNER JOIN LATERAL (
                        SELECT handling
                        FROM varsel_handling WHERE vk.id = varsel_handling.varselkode_ref
                        ORDER BY varsel_handling.opprettet DESC LIMIT 1
                        ) vh ON true;
                """

                tx.run(queryOf(query).map { row ->
                    Varsel(
                        row.string(1),
                        row.string(2),
                        row.stringOrNull(3),
                        row.stringOrNull(4),
                        row.boolean(5)
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

    private fun TransactionalSession.nyVarseltittel(kodeRef: Long, tittel: String): Boolean {
        if (sisteTittel(kodeRef) == tittel) return false
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES (?, ?)"
        return run(queryOf(query, kodeRef, tittel).asUpdateAndReturnGeneratedKey) != null
    }

    private fun TransactionalSession.nyVarselforklaring(kodeRef: Long, forklaring: String?): Boolean {
        if (sisteForklaring(kodeRef) == forklaring) return false
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES (?, ?)"
        return run(queryOf(query, kodeRef, forklaring).asUpdateAndReturnGeneratedKey) != null
    }

    private fun TransactionalSession.nyVarselhandling(kodeRef: Long, handling: String?): Boolean {
        if (sisteHandling(kodeRef) == handling) return false
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

    internal class IngenEndring(kode: String): VarselException("Varsel for varselkode $kode ikke endret, da det payload er lik det som finnes i databasen") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotModified
    }
}