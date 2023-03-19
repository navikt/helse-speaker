package no.nav.helse.speaker

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.routing.routing
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.db.DataSourceBuilder
import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.domene.ActualVarselRepository
import no.nav.helse.speaker.microsoft.AzureAD
import no.nav.helse.speaker.microsoft.MsGraphClient
import no.nav.helse.speaker.plugins.configureAuthentication
import no.nav.helse.speaker.plugins.configureServerContentNegotiation
import no.nav.helse.speaker.plugins.configureUtilities
import no.nav.helse.speaker.plugins.statusPages
import no.nav.helse.speaker.routes.speaker

internal fun main() {
    RapidApp(System.getenv()).start()
}

private class RapidApp(env: Map<String, String>) {
    private lateinit var rapidsConnection: RapidsConnection
    private val app = App(env,
        {
            MsGraphClient(
                azureAD = it,
                groupId = env.getValue("AZURE_VALID_GROUP_ID"),
                graphUrl = env.getValue("MS_GRAPH_BASE_URL")
            )
        }
    ) { rapidsConnection }

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
    msGraphClient: (AzureAD) -> IMsGraphClient,
    rapidsConnection: () -> RapidsConnection,
) : RapidsConnection.StatusListener {
    private val azureAD = AzureAD.fromEnv(env)
    private val msGraphClient = msGraphClient(azureAD)
    private val rapidsConnection: RapidsConnection by lazy { rapidsConnection() }
    private val dataSourceBuilder = DataSourceBuilder(env)
    private val dao = VarseldefinisjonDao(dataSourceBuilder.getDataSource())
    private val repository = ActualVarselRepository(dao)
    private val mediator: Mediator = Mediator(rapidsConnection, repository, this.msGraphClient)

    internal fun ktorApp(application: Application) = application.app(env, mediator, azureAD)
    internal fun start() {
        rapidsConnection.register(this)
        rapidsConnection.start()
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        dataSourceBuilder.migrate()
    }
}

internal fun Application.app(
    env: Map<String, String>,
    mediator: Mediator,
    azureAD: AzureAD
) {
    val isLocalDevelopment = env["LOCAL_DEVELOPMENT"]?.toBooleanStrict() ?: false
    statusPages()
    configureUtilities()
    configureServerContentNegotiation()
    configureAuthentication(azureAD)
    routing {
        authenticate("ValidToken") {
            singlePageApplication {
                useResources = !isLocalDevelopment
                react("speaker-frontend/dist")
                ignoreFiles { it.endsWith(".txt") }
            }
            speaker(azureAD.issuer(), mediator)
        }
    }
}