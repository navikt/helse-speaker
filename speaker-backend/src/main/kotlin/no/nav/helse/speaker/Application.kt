package no.nav.helse.speaker

import io.ktor.server.application.Application
import io.ktor.server.application.ServerReady
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.db.DataSourceBuilder
import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.domene.ActualVarselRepository
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon.Companion.konverterTilVarselkode
import no.nav.helse.speaker.plugins.*
import no.nav.helse.speaker.routes.nais
import no.nav.helse.speaker.routes.speaker

internal fun main() {
    createApp(System.getenv())
}

internal fun createApp(env: Map<String, String>) {
    val dataSourceBuilder = DataSourceBuilder(env)
    val dao = VarseldefinisjonDao(dataSourceBuilder.getDataSource())
    val repository = ActualVarselRepository(dao)
    val server = embeddedServer(Netty, applicationEngineEnvironment {
        module {
            environment.monitor.subscribe(ServerReady) { _ ->
                dataSourceBuilder.migrate()
                val varselkoder = dao.finnAlleDefinisjoner().konverterTilVarselkode(dataSourceBuilder.getDataSource())
                varselkoder.forEach {
                    dao.opprett(it)
                }
            }
            app(repository, env)
        }
        connector {
            port = 8080
        }
    })

    server.start(wait = true)
}

internal fun Application.app(repository: VarselRepository, env: Map<String, String>) {
    val isLocalDevelopment = env["LOCAL_DEVELOPMENT"]?.toBooleanStrict() ?: false
    statusPages()
    configureUtilities()
    configureServerContentNegotiation()
    metrics()
    nais()
    if (erDev() || isLocalDevelopment) {
        dev(repository, env, isLocalDevelopment)
    }
}

private fun Application.dev(repository: VarselRepository, env: Map<String, String>, isLocalDevelopment: Boolean) {
    val azureAD = AzureAD.fromEnv(env)
    configureAuthentication(azureAD)
    routing {
        authenticate("ValidToken") {
            singlePageApplication {
                useResources = !isLocalDevelopment
                react("speaker-frontend/dist")
                ignoreFiles { it.endsWith(".txt") }
            }
            speaker(azureAD, repository)
        }
    }
}

private fun erDev() = "dev-gcp" == System.getenv("NAIS_CLUSTER_NAME")