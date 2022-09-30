package no.nav.helse.speaker

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.helse.speaker.db.DataSourceBuilder
import no.nav.helse.speaker.db.VarselDao
import no.nav.helse.speaker.plugins.configureRouting

internal fun main() {
    val dataSourceBuilder = DataSourceBuilder(System.getenv())
    val dao = VarselDao(dataSourceBuilder.getDataSource())
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
