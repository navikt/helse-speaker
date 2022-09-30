package no.nav.helse.speaker.db

import io.ktor.http.HttpStatusCode
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class VarselDao(private val dataSource: DataSource) {

    internal fun nyttVarsel(kode: String, tittel: String, forklaring: String?, handling: String?, avviklet: Boolean) {
        sessionOf(dataSource, returnGeneratedKey = true).use {
            it.transaction { tx ->
                val kodeRef = tx.nyVarselkode(kode, avviklet) ?: throw VarselException.KodeEksisterer(kode)
                tx.nyVarseltittel(kodeRef, tittel)
                forklaring?.also { tx.nyVarselforklaring(kodeRef, forklaring) }
                handling?.also { tx.nyVarselhandling(kodeRef, handling) }
            }
        }
    }

    private fun TransactionalSession.nyVarselkode(kode: String, avviklet: Boolean): Long? {
        @Language("PostgreSQL")
        val query = "INSERT INTO varselkode(kode, avviklet) VALUES (?, ?) ON CONFLICT(kode) DO NOTHING"
        return run(queryOf(query, kode, avviklet).asUpdateAndReturnGeneratedKey)
    }

    private fun TransactionalSession.nyVarseltittel(kodeRef: Long, tittel: String): Long? {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES (?, ?) ON CONFLICT(varselkode_ref, tittel) DO NOTHING"
        return run(queryOf(query, kodeRef, tittel).asUpdateAndReturnGeneratedKey)
    }

    private fun TransactionalSession.nyVarselforklaring(kodeRef: Long, forklaring: String?): Long? {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES (?, ?) ON CONFLICT(varselkode_ref, forklaring) DO NOTHING"
        return run(queryOf(query, kodeRef, forklaring).asUpdateAndReturnGeneratedKey)
    }

    private fun TransactionalSession.nyVarselhandling(kodeRef: Long, handling: String?): Long? {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_handling(varselkode_ref, handling) VALUES (?, ?) ON CONFLICT(varselkode_ref, handling) DO NOTHING"
        return run(queryOf(query, kodeRef, handling).asUpdateAndReturnGeneratedKey)
    }
}

internal sealed class VarselException(message: String): Exception(message) {
    protected abstract val httpStatusCode: HttpStatusCode

    internal class KodeEksisterer(kode: String): VarselException("Varselkode $kode eksisterer allerede") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.Conflict
    }
}