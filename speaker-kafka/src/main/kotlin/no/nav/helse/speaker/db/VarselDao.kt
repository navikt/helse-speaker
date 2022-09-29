package no.nav.helse.speaker.db

import com.fasterxml.jackson.databind.JsonNode
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.Varsel
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal class VarselDao(private val dataSource: DataSource) {

    internal fun byggVarsel(
        id: UUID,
        kode: String,
        melding: String,
        kontekster: List<JsonNode>,
        tidsstempel: LocalDateTime
    ): Varsel {
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                val gyldig = tx.valider(kode)
                if (!gyldig) return Varsel.ugyldig(id, melding, kode, kontekster, tidsstempel)

                val tittel = tx.finnTittel(kode)
                val forklaring = tx.finnForklaring(kode)
                val handling = tx.finnHandling(kode)
                val avviklet = tx.finnAvviklet(kode)
                return Varsel.gyldig(id, tittel, kode, kontekster, tidsstempel, forklaring, handling, avviklet)
            }
        }
    }

    private fun TransactionalSession.valider(kode: String): Boolean {
        @Language("PostgreSQL")
        val query = "SELECT 1 FROM varselkode WHERE kode = ?"
        return this.run(queryOf(query, kode).map { it.boolean(1) }.asSingle) ?: false
    }

    private fun TransactionalSession.finnTittel(kode: String): String {
        @Language("PostgreSQL")
        val query = "SELECT tittel FROM varsel_tittel vt INNER JOIN varselkode v on v.id = vt.varselkode_ref WHERE v.kode = ? ORDER BY vt.id DESC"
        return requireNotNull(this.run(queryOf(query, kode).map { it.string(1) }.asSingle)) { "Fant ikke tittel for varsel med kode=$kode" }
    }

    private fun TransactionalSession.finnForklaring(kode: String): String? {
        @Language("PostgreSQL")
        val query = "SELECT forklaring FROM varsel_forklaring vf INNER JOIN varselkode v on v.id = vf.varselkode_ref WHERE v.kode = ? ORDER BY vf.id DESC"
        return this.run(queryOf(query, kode).map { it.string(1) }.asSingle)
    }

    private fun TransactionalSession.finnHandling(kode: String): String? {
        @Language("PostgreSQL")
        val query = "SELECT handling FROM varsel_handling vh INNER JOIN varselkode v on v.id = vh.varselkode_ref WHERE v.kode = ? ORDER BY vh.id DESC"
        return this.run(queryOf(query, kode).map { it.string(1) }.asSingle)
    }

    private fun TransactionalSession.finnAvviklet(kode: String): Boolean {
        @Language("PostgreSQL")
        val query = "SELECT avviklet FROM varselkode WHERE kode = ?"
        return requireNotNull(this.run(queryOf(query, kode).map { it.boolean(1) }.asSingle)) { "Fant ikke informasjon om koden er avviklet for kode=$kode" }
    }
}