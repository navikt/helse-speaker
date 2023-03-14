package no.nav.helse.speaker

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.domene.IVarselkodeObserver
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.slf4j.LoggerFactory

internal class Mediator(private val rapidsConnection: RapidsConnection): IVarselkodeObserver {
    override fun varselkodeOppdatert(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
        sikkerlogg.info("Publiserer oppdatert definisjon for {}", kv("varselkode", kode))
        rapidsConnection.publish(
            JsonMessage.newMessage(
                "varselkode_ny_definisjon",
                mapOf(
                    "varselkode" to kode,
                    "gjeldende_definisjon" to gjeldendeDefinisjon.toMap()
                )
            ).toJson()
        )
    }

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
}
