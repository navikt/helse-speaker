package no.nav.helse.speaker

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.helse.speaker.db.DataSourceBuilder
import no.nav.helse.speaker.db.VarselDao
import no.nav.helse.speaker.plugins.configureRestApi
import no.nav.helse.speaker.plugins.configureSerialization

internal fun main() {
    createApp(System.getenv())
}

internal fun createApp(env: Map<String, String>) {
    val dataSourceBuilder = DataSourceBuilder(env)
    val dao = VarselDao(dataSourceBuilder.getDataSource())
    val repository = ActualVarselRepository(dao)
    dataSourceBuilder.migrate()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureRestApi(repository)
    }.start(wait = true)
}
