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
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.plugins.configureAuthentication
import no.nav.helse.speaker.plugins.configureServerContentNegotiation
import no.nav.helse.speaker.plugins.configureUtilities
import no.nav.helse.speaker.plugins.statusPages
import no.nav.helse.speaker.routes.speaker

internal fun main() {
    RapidApp(System.getenv() + mapOf(
        "KAFKA_RAPID_TOPIC" to "noe",
        "KAFKA_BOOTSTRAP_SERVERS" to "nav-integration-test-kafka-nav-integration-test.aivencloud.com:26484",
        "KAFKA_CONSUMER_GROUP_ID" to "noe"
    )).start()
}

private class RapidApp(env: Map<String, String>) {
    private lateinit var rapidsConnection: RapidsConnection
    private val app = App(env) { rapidsConnection }

    init {
        rapidsConnection = RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(env))
            .withKtorModule {
                app.ktorApp(this)
            }.build()
    }

    fun start() = app.start()
}

internal class App(
    private val env: Map<String, String>,
    rapidsConnection: () -> RapidsConnection
): RapidsConnection.StatusListener {
    private val rapidsConnection: RapidsConnection by lazy { rapidsConnection() }
    private val dataSourceBuilder = DataSourceBuilder(env)
    private val dao = VarseldefinisjonDao(dataSourceBuilder.getDataSource())
    private val repository = ActualVarselRepository(dao)
    private val mediator: Mediator = Mediator(rapidsConnection, repository)

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
    mediator: Mediator
) {
    val isLocalDevelopment = env["LOCAL_DEVELOPMENT"]?.toBooleanStrict() ?: false
    statusPages()
    configureUtilities()
    configureServerContentNegotiation()
    if (erDev() || isLocalDevelopment) {
        dev(repository, env, isLocalDevelopment, mediator)
    }
}

private fun Application.dev(
    repository: VarselRepository,
    env: Map<String, String>,
    isLocalDevelopment: Boolean,
    mediator: Mediator
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
            speaker(azureAD, repository, mediator)
        }
    }
}

private fun erDev() = "dev-gcp" == System.getenv("NAIS_CLUSTER_NAME")