package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
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
    private val avviklet: Boolean
) {
    private fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "kode" to kode,
            "tittel" to tittel,
            "forklaring" to forklaring,
            "handling" to handling,
            "avviklet" to avviklet,
            "tidsstempel" to tidsstempel,
            "kontekster" to kontekster
        )
    }

    internal fun erGyldig() = gyldigKode

    internal companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        internal fun List<Varsel>.toJson(): List<Map<String, Any?>> = map { it.toJson() }

        internal fun gyldig(id: UUID, tittel: String, kode: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime, beskrivelse: String?, handling: String?, deprecated: Boolean): Varsel {
            return Varsel(id, tittel, kode, true, kontekster, tidsstempel, beskrivelse, handling, deprecated)
        }
        internal fun ugyldig(id: UUID, tittel: String, kode: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): Varsel {
            return Varsel(id, tittel, kode, false, kontekster, tidsstempel, null, null, false)
        }

        internal fun List<Varsel>.loggUgyldig() {
            filterNot(Varsel::erGyldig).forEach {
                sikkerlogg.info("Varsel {} med {} benytter ikke en gyldig kode.", keyValue("id", it.id), keyValue("kode", it.kode))
            }
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