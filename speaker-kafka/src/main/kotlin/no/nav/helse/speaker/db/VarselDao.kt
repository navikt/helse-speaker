package no.nav.helse.speaker.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.UgyldigVarsel
import no.nav.helse.speaker.Varseldefinisjon
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal class VarselDao(private val dataSource: DataSource) {

    internal fun sjekkGyldighet(
        id: UUID,
        kode: String,
        melding: String,
        tidsstempel: LocalDateTime
    ): UgyldigVarsel? {
        return sessionOf(dataSource).use { session ->
            if (!session.sjekkGyldighet(kode)) return UgyldigVarsel(id, tidsstempel, kode, melding)
            null
        }
    }

    private fun Session.sjekkGyldighet(kode: String): Boolean {
        @Language("PostgreSQL")
        val query = "SELECT 1 FROM varselkode WHERE kode = ?"
        return this.run(queryOf(query, kode).map { it.boolean(1) }.asSingle) ?: false
    }

    internal fun finnDefinisjoner(): List<Varseldefinisjon> {
        return sessionOf(dataSource).use {
            it.transaction { tx ->
                @Language("PostgreSQL")
                val query = """
                SELECT vk.kode, unik_id, tittel, forklaring, handling, vk.avviklet, vk.opprettet
                FROM varselkode vk INNER JOIN LATERAL (
                    SELECT tittel, unik_id, forklaring, handling, varselkode_ref FROM varsel_definisjon vd WHERE vd.varselkode_ref = vk.id ORDER BY vd.id DESC LIMIT 1
                ) rad ON rad.varselkode_ref = vk.id
                """

                tx.run(queryOf(query).map { row ->
                    Varseldefinisjon(
                        unikId = row.uuid("unik_id"),
                        kode = row.string("kode"),
                        tittel = row.string("tittel"),
                        forklaring = row.stringOrNull("forklaring"),
                        handling = row.stringOrNull("handling"),
                        avviklet = row.boolean("avviklet"),
                        opprettet = row.localDateTime("opprettet")
                    ) }.asList)
            }
        }
    }
}