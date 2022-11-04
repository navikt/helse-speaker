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
                SELECT vk.kode, tittel, forklaring, handling, vk.avviklet, vk.opprettet
                FROM varselkode vk INNER JOIN LATERAL (
                    SELECT tittel, forklaring, handling, t.varselkode_ref, greatest(t.opprettet, f.opprettet, h.opprettet) as opprettet FROM
                        (
                        SELECT tittel, varselkode_ref, t.opprettet FROM varsel_tittel t
                        WHERE vk.id = t.varselkode_ref ORDER BY t.opprettet DESC LIMIT 1
                        ) t INNER JOIN
                        (
                        SELECT forklaring, varselkode_ref, f.opprettet FROM varsel_forklaring f
                        WHERE vk.id = f.varselkode_ref ORDER BY f.opprettet DESC LIMIT 1
                        ) f ON f.varselkode_ref = vk.id INNER JOIN
                        (
                        SELECT handling, varselkode_ref, h.opprettet FROM varsel_handling h
                        WHERE vk.id = h.varselkode_ref ORDER BY h.opprettet DESC LIMIT 1
                        ) h ON h.varselkode_ref = vk.id
                ) rad ON rad.varselkode_ref = vk.id
                """

                tx.run(queryOf(query).map { row ->
                    Varseldefinisjon(
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