package no.nav.helse.speaker

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.Varseldefinisjon.Companion.toJson
import no.nav.helse.speaker.db.DataSourceBuilder
import org.slf4j.LoggerFactory

internal fun main() {
    val app = createApp(System.getenv())
    app.start()
}

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

private fun createApp(env: Map<String, String>): RapidsConnection {
    val dataSourceBuilder = DataSourceBuilder(env)
    val dataSource by lazy { dataSourceBuilder.getDataSource() }
    return RapidApplication.create(env).apply {
        val varselRepository = ActualVarselRepository { dataSource }
        VarselRiver(this, varselRepository)
        val definisjoner = JsonMessage.newMessage("varseldefinisjoner_endret", varselRepository.definisjoner().toJson())
        val definisjonerJson = definisjoner.toString()
        this.publish(definisjonerJson)
        sikkerlogg.info("Publiserer definisjoner for varsler: {}", keyValue("definisjoner", definisjonerJson))
        register(object: RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                dataSource.close()
            }
        })
    }
}