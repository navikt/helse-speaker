package no.nav.helse.speaker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ServerReady
import io.ktor.server.application.call
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import no.nav.helse.speaker.db.DataSourceBuilder
import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.plugins.configureRestApi
import no.nav.helse.speaker.plugins.configureSerialization
import kotlin.system.exitProcess

internal fun main() {
    createApp(System.getenv())
}

internal fun createApp(env: Map<String, String>) {
    val dataSourceBuilder = DataSourceBuilder(env)
    val dao = VarseldefinisjonDao(dataSourceBuilder.getDataSource())
    val repository = ActualVarselRepository(dao)
    val dings = embeddedServer(Netty, applicationEngineEnvironment {
        module {
            routing {
                get("/isready") {
                    call.respond(HttpStatusCode.OK)
                }
                get("/isalive") {
                    call.respond(HttpStatusCode.OK)
                }
                //configureRestApi(repository)
            }
            environment.monitor.subscribe(ServerReady) { application ->
                dataSourceBuilder.migrate()
            }
            //configureSerialization()
        }
        connector {
            port = 8080
        }
    })
    dings.start(wait = true)
}
