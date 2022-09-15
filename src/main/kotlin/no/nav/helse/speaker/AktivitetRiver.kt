package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.speaker.Varsel.Companion.toJson
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

internal class AktivitetRiver(rapidsConnection: RapidsConnection, private val varselRepository: VarselRepository = VarselRepository()): River.PacketListener {

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
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
        val varsler = packet["aktiviteter"].filter { it["nivå"].asText() == "VARSEL" }.map {
            val id = UUID.fromString(it["id"].asText())
            val melding = it["melding"].asText()
            val kontekster = it["kontekster"].toList()
            val tidsstempel = LocalDateTime.parse(it["tidsstempel"].asText())
            val kode = it["varselkode"]?.asText() ?: return
            varselRepository.opprett(id, kode, melding, kontekster, tidsstempel)
        }
        if (varsler.isEmpty()) return
        sikkerlogg.info("Publiserer ${varsler.size} varsler for {}", keyValue("fødselsnummer", fødselsnummer))
        publiserVarsler(fødselsnummer, varsler, context)
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