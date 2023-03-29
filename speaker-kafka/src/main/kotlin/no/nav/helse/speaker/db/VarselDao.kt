package no.nav.helse.speaker.db

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.Varselkode
import org.intellij.lang.annotations.Language
import java.util.*
import javax.sql.DataSource

internal class VarselDao(private val dataSource: DataSource) {

    private val json = Json { ignoreUnknownKeys = true }

    internal fun finnVarselkoderFor(varselkoder: List<String>): List<Varselkode> {
        if (varselkoder.isEmpty()) return emptyList()
        @Language("PostgreSQL")
        val query = "SELECT json FROM varseldefinisjon WHERE varselkode IN (${varselkoder.joinToString { "?" }})"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, *varselkoder.toTypedArray()).map { row ->
                json.decodeFromString<Varselkode>(row.string("json"))
            }.asList)
        }
    }
}