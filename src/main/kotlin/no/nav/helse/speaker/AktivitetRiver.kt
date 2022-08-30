package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory

class AktivitetRiver(rapidsConnection: RapidsConnection): River.PacketListener {

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "aktivitetslogg_ny_aktivitet")
                it.requireKey("fødselsnummer", "@id", "@opprettet")
                it.requireArray("aktiviteter") {
                    requireKey("melding", "nivå")
                    require("tidsstempel", JsonNode::asLocalDateTime)
                    requireArray("kontekster") {
                        requireKey("konteksttype", "kontekstmap")
                    }
                }
            }
        }.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val varsler = packet["aktiviteter"].filter { it["nivå"].asText() == "VARSEL" }
        if (varsler.isEmpty()) return
        sikkerlogg.info("Publiserer ${varsler.size} varsler for {}", keyValue("fødselsnummer", fødselsnummer))
        publiserVarsler(fødselsnummer, varsler, context)
    }

    private fun publiserVarsler(fødselsnummer: String, varsler: List<JsonNode>, context: MessageContext) {
        val melding = JsonMessage.newMessage(
            "aktivitetslogg_nye_varsler",
            mapOf(
                "fødselsnummer" to fødselsnummer,
                "varsler" to varsler
            )
        ).toJson()
        context.publish(fødselsnummer, melding)
    }
}