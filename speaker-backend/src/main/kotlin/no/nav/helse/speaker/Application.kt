package no.nav.helse.speaker

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.routing.routing
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.db.DataSourceBuilder
import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.domene.ActualVarselRepository
import no.nav.helse.speaker.domene.IVarselkodeObserver
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.plugins.configureAuthentication
import no.nav.helse.speaker.plugins.configureServerContentNegotiation
import no.nav.helse.speaker.plugins.configureUtilities
import no.nav.helse.speaker.plugins.statusPages
import no.nav.helse.speaker.routes.speaker

internal fun main() {
    RapidApp(System.getenv()).start()
}

private class RapidApp(env: Map<String, String>) {
    private lateinit var rapidConnection: RapidsConnection
    private val app = App(env, rapidConnection)

    init {
        rapidConnection = RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(env))
            .withKtorModule {
                app.ktorApp(this)
            }.build()
    }

    fun start() = app.start()
}

internal class App(
    private val env: Map<String, String>,
    private val rapidsConnection: RapidsConnection
): RapidsConnection.StatusListener {
    private val mediator: Mediator = Mediator(rapidsConnection)
    private val dataSourceBuilder = DataSourceBuilder(env)
    private val dao = VarseldefinisjonDao(dataSourceBuilder.getDataSource())
    private val repository = ActualVarselRepository(dao)

    internal fun ktorApp(application: Application) = application.app(repository, env, mediator)
    internal fun start() {
        rapidsConnection.start()
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        dataSourceBuilder.migrate()
    }
}

internal fun Application.app(
    repository: VarselRepository,
    env: Map<String, String>,
    vararg observere: IVarselkodeObserver
) {
    val isLocalDevelopment = env["LOCAL_DEVELOPMENT"]?.toBooleanStrict() ?: false
    statusPages()
    configureUtilities()
    configureServerContentNegotiation()
    if (erDev() || isLocalDevelopment) {
        dev(repository, env, isLocalDevelopment, *observere)
    }
}

private fun Application.dev(
    repository: VarselRepository,
    env: Map<String, String>,
    isLocalDevelopment: Boolean,
    vararg observere: IVarselkodeObserver
) {
    val azureAD = AzureAD.fromEnv(env)
    configureAuthentication(azureAD)
    routing {
        authenticate("ValidToken") {
            singlePageApplication {
                useResources = !isLocalDevelopment
                react("speaker-frontend/dist")
                ignoreFiles { it.endsWith(".txt") }
            }
            speaker(azureAD, repository, *observere)
        }
    }
}

private fun erDev() = "dev-gcp" == System.getenv("NAIS_CLUSTER_NAME")