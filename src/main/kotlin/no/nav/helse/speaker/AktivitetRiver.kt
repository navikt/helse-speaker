package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.speaker.Varsel.Companion.toJson
import no.nav.helse.speaker.VarselRepository.Companion.varsler
import org.slf4j.LoggerFactory

internal class AktivitetRiver(
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
        val fødselsnummer = packet["fødselsnummer"].asText()
        val (varslerMedKode, varslerUtenKode) = packet["aktiviteter"]
            .filter { it["nivå"].asText() == "VARSEL" }
            .partition { it["varselkode"]?.asText() != null }
        varslerUtenKode.logg()

        if (varslerMedKode.isEmpty()) return
        sikkerlogg.info("Publiserer ${varslerMedKode.size} varsler for {}", keyValue("fødselsnummer", fødselsnummer))
        publiserVarsler(fødselsnummer, varslerMedKode.varsler(varselRepository), context)
    }

    private fun publiserVarsler(fødselsnummer: String, varsler: List<Varsel>, context: MessageContext) {
        val melding = JsonMessage.newMessage(
            "aktivitetslogg_nye_varsler",
            mapOf(
                "fødselsnummer" to fødselsnummer,
                "varsler" to varsler.toJson()
            )
        ).toJson()
        context.publish(fødselsnummer, melding)
    }
}