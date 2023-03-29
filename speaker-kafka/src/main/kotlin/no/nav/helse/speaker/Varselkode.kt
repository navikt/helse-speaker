@file:UseSerializers(UUIDSerializer::class, LocalDateTimeSerializer::class)
package no.nav.helse.speaker

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.speaker.felles.LocalDateTimeSerializer
import no.nav.helse.speaker.felles.UUIDSerializer
import org.slf4j.LoggerFactory

@Serializable
internal class Varselkode(
    private val kode: String,
    private val definisjoner: List<Definisjon>
) {
    internal fun avviklet() = definisjoner.last().erAvviklet()

    internal companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        internal fun List<Varselkode>.loggUgyldige() {
            // TODO: Slack-integrasjon
            filter { it.avviklet() }.forEach {
                sikkerlogg.info("Varselkode {} er avviklet men blir produsert på kafka", keyValue("kode", it.kode))
            }
        }
    }
}

@Serializable
internal class Definisjon(
    private val avviklet: Boolean
) {
    internal fun erAvviklet() = avviklet
}