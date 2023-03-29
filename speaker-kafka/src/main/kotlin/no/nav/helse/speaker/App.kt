package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.db.DataSourceBuilder

internal fun main() {
    val app = createApp(System.getenv())
    app.start()
}

private fun createApp(env: Map<String, String>): RapidsConnection {
    val dataSourceBuilder = DataSourceBuilder(env)
    val dataSource by lazy { dataSourceBuilder.getDataSource() }
    return RapidApplication.create(env).apply {
        val varselRepository = ActualVarselRepository { dataSource }
        VarselRiver(this, varselRepository)
        register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                dataSource.close()
            }
        })
    }
}