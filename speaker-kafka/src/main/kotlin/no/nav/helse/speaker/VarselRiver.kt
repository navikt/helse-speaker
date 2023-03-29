package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.speaker.Varselkode.Companion.loggUgyldige
import org.slf4j.LoggerFactory

internal class VarselRiver(
    rapidsConnection: RapidsConnection,
    private val varselRepository: VarselRepository
): River.PacketListener {

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private fun List<JsonNode>.logg() {
            forEach {
                sikkerlogg.info("Varsel \"${it["melding"].asText()}\" med {} mangler kode", keyValue("id", it["id"].asText()))
            }
        }
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "aktivitetslogg_ny_aktivitet")
                it.requireKey("fødselsnummer", "@id", "@opprettet")
                it.requireArray("aktiviteter") {
                    requireKey("melding", "nivå", "id")
                    interestedIn("varselkode")
                    require("tidsstempel", JsonNode::asLocalDateTime)
                    requireArray("kontekster") {
                        requireKey("konteksttype", "kontekstmap")
                    }
                }
            }
        }.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val (varslerMedKode, varslerUtenKode) = packet["aktiviteter"]
            .filter { it["nivå"].asText() == "VARSEL" }
            .partition { it["varselkode"]?.asText() != null }
        varslerUtenKode.logg()
        val varselkoderStrings = varslerMedKode.map { it["varselkode"].asText() }
        val varselkoder = varselRepository.varselkoderFor(varselkoderStrings)
        varselkoder.loggUgyldige()
    }
}