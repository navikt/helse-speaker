package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*

class Varsel private constructor(
    private val id: UUID,
    private val tittel: String,
    private val kode: String,
    private val gyldigKode: Boolean,
    private val kontekster: List<JsonNode>,
    private val tidsstempel: LocalDateTime,
    private val forklaring: String?,
    private val handling: String?,
    private val deprecated: Boolean
) {
    private fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "tittel" to tittel,
            "kode" to kode,
            "tidsstempel" to tidsstempel,
            "kontekster" to kontekster
        )
    }

    internal fun erGyldig() = gyldigKode

    internal companion object {
        internal fun List<Varsel>.toJson(): List<Map<String, Any>> = map { it.toJson() }

        internal fun gyldig(id: UUID, tittel: String, kode: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime, beskrivelse: String?, handling: String?, deprecated: Boolean): Varsel {
            return Varsel(id, tittel, kode, true, kontekster, tidsstempel, beskrivelse, handling, deprecated)
        }
        internal fun ugyldig(id: UUID, tittel: String, kode: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): Varsel {
            return Varsel(id, tittel, kode, false, kontekster, tidsstempel, null, null, false)
        }

        internal fun JsonNode.fromJson(repository: VarselRepository): Varsel {
            val id = UUID.fromString(this["id"].asText())
            val melding = this["melding"].asText()
            val kontekster = this["kontekster"].toList()
            val tidsstempel = LocalDateTime.parse(this["tidsstempel"].asText())
            val kode = this["varselkode"].asText()
            return repository.opprett(id, kode, melding, kontekster, tidsstempel)
        }
    }
}