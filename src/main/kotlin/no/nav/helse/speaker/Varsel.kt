package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*

class Varsel(
    private val id: UUID,
    private val tittel: String,
    private val kode: String,
    private val kontekster: List<JsonNode>,
    private val tidsstempel: LocalDateTime,
    private val beskrivelse: String?,
    private val handling: String?,
    private val gyldig: Boolean,
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

    internal companion object {
        internal fun List<Varsel>.toJson(): List<Map<String, Any>> = map { it.toJson() }
    }
}