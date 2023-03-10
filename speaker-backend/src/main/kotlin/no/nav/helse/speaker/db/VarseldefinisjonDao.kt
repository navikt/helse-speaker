package no.nav.helse.speaker.db

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.intellij.lang.annotations.Language
import java.util.*
import javax.sql.DataSource

internal class VarseldefinisjonDao(private val dataSource: DataSource) {

    internal fun finn(kode: String): Varselkode? {
        @Language("PostgreSQL")
        val query = "SELECT json FROM varseldefinisjon WHERE varselkode = :varselkode"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, mapOf("varselkode" to kode)).map {
                Json.decodeFromString<Varselkode>(it.string("json"))
            }.asSingle)
        }
    }

    internal fun oppdater(varselkode: Varselkode) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query =
                "UPDATE varseldefinisjon SET json = :json::json WHERE varselkode = :varselkode"
            session.run(queryOf(query, mapOf(
                "json" to Json.encodeToString(varselkode),
                "varselkode" to varselkode.kode()
            )).asUpdate)
        }
    }

    internal fun opprett(varselkode: Varselkode) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query =
                "INSERT INTO varseldefinisjon(varselkode, json) VALUES (:varselkode, :json::json)"
            session.run(queryOf(query, mapOf(
                "json" to Json.encodeToString(varselkode),
                "varselkode" to varselkode.kode()
            )).asUpdate)
        }
    }

    internal fun finnAlleDefinisjoner(): List<Varseldefinisjon> {
        return sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                @Language("PostgreSQL")
                val query = """
                SELECT vk.kode, tittel, forklaring, handling, vk.avviklet, unik_id, vd.opprettet
                FROM varselkode vk INNER JOIN varsel_definisjon vd ON vk.id = vd.varselkode_ref ORDER BY vd.id"""

                tx.run(queryOf(query).map { row ->
                    Varseldefinisjon(
                        id = row.uuid("unik_id"),
                        varselkode = row.string("kode"),
                        tittel = row.string("tittel"),
                        forklaring = row.stringOrNull("forklaring"),
                        handling = row.stringOrNull("handling"),
                        avviklet = row.boolean("avviklet"),
                        opprettet = row.localDateTime("opprettet")
                    )
                }.asList)
            }
        }
    }


    internal fun finnVarselkoder(): Set<Varselkode> {
        @Language("PostgreSQL")
        val query = "SELECT json FROM varseldefinisjon"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { Json.decodeFromString<Varselkode>(it.string("json")) }.asList)
        }.toSet()
    }
}

internal sealed class VarselException(message: String) : Exception(message) {
    internal abstract val httpStatusCode: HttpStatusCode

    internal class IngenEndring(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varseldefinisjon $varseldefinisjon er allerede gjeldende definisjon") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotModified
    }
}