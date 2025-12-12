package no.nav.helse.speaker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal val logg = LoggerFactory.getLogger("Speaker")

fun main() {
    val env = System.getenv()
    val bøttenavn = env.requiredValue("BUCKET_NAME")

    app(env, Kafka, GCPBøtte(bøttenavn))
}

fun app(
    env: Map<String, String>,
    sender: Sender,
    bøtte: Bøtte
) {
    val sanityProjectId = env.requiredValue("SANITY_PROJECT_ID")
    val sanityDataset = env.requiredValue("SANITY_DATASET")
    val iProduksjonsmiljø = env["NAIS_CLUSTER_NAME"] == "prod-gcp"

    logg.info("Svarer på isalive og isready")
    var up = false

    val exceptionHandler =
        CoroutineExceptionHandler { coroutineContext, exception ->
            logg.error("En feil har oppstått: ${exception.localizedMessage}", exception)
            coroutineContext.cancel(CancellationException("En feil har oppstått", exception))
        }
    val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)
    scope.launch {
        up = true
        sanityVarselendringerListener(iProduksjonsmiljø, sanityProjectId, sanityDataset, sender, bøtte)
        up = false // lytteren returnerte og klienten er derfor ikke lenger kjørende
    }
    val server = scope.embeddedServer(CIO, port = 8080) {
        routing {
            get("/isalive") {
                if (up) call.respondText("ALIVE!")
                else call.respondText("NOT ALIVE!", status = HttpStatusCode.ServiceUnavailable)
            }
            get("/isready") { call.respondText("READY!") }
        }
        monitor.subscribe(ApplicationStopped) {
            logg.info("Avslutter appen")
        }
    }
    server.settOppShutdownHook()
    server.start(wait = true)
}

fun Map<String, String>.requiredValue(key: String) = requireNotNull(get(key)) {
    "Key $key mangler i miljøvariablene"
}

private fun EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>.settOppShutdownHook() {
    Runtime.getRuntime().addShutdownHook(Thread {
        stop(1, 5, TimeUnit.SECONDS)
    })
}
