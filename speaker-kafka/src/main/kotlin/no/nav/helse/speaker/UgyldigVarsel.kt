package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

internal class Varseldefinisjon(
    private val kode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean,
    private val opprettet: LocalDateTime
) {
    internal companion object {
        internal fun List<Varseldefinisjon>.toJson(): Map<String, Any> {
            return mapOf(
                "definisjoner" to map {
                    mapOf(
                        "kode" to it.kode,
                        "tittel" to it.tittel,
                        "forklaring" to it.forklaring,
                        "handling" to it.handling,
                        "avviklet" to it.avviklet,
                        "opprettet" to it.opprettet
                    )
                }
            )
        }
    }
}

internal class UgyldigVarsel(
    private val id: UUID,
    private val tidsstempel: LocalDateTime,
    private val kode: String,
    private val tittel: String
) {
    internal fun logg() {
        sikkerlogg.info(
            "Varsel {}, {} med {} og tittel=\"$tittel\" benytter ikke en gyldig kode.",
            keyValue("opprettet", tidsstempel),
            keyValue("id", id),
            keyValue("kode", kode)
        )
    }

    internal companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

        internal fun List<UgyldigVarsel>.loggUgyldige() {
            forEach(UgyldigVarsel::logg)
        }

        internal fun JsonNode.fromJson(repository: VarselRepository): UgyldigVarsel? {
            val id = UUID.fromString(this["id"].asText())
            val melding = this["melding"].asText()
            val kontekster = this["kontekster"].toList()
            val tidsstempel = LocalDateTime.parse(this["tidsstempel"].asText())
            val kode = this["varselkode"].asText()
            return repository.sjekkGyldighet(id, kode, melding, kontekster, tidsstempel)
        }
    }
}