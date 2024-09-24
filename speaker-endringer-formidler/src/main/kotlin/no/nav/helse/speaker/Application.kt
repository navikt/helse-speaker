package no.nav.helse.speaker

import io.ktor.server.application.ApplicationStopped
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
internal val logg = LoggerFactory.getLogger("SpeakerEndringerFormidler")

fun main() {
    val env = System.getenv()
    val bøttenavn = env["BUCKET_NAME"] ?: throw IllegalArgumentException("Mangler bucket name")
    val sender = Kafka

    app(env, sender, GCPBøtte(bøttenavn))
}

fun app(
    env: Map<String, String>,
    sender: Sender,
    bøtte: Bøtte
) {
    val sanityProjectId = env["SANITY_PROJECT_ID"] ?: throw IllegalArgumentException("Mangler sanity projectId")
    val sanityDataset = env["SANITY_DATASET"] ?: throw IllegalArgumentException("Mangler sanity dataset")
    val iProduksjonsmiljø = env["NAIS_CLUSTER_NAME"] == "prod-gcp"

    logg.info("Svarer på isalive og isready")
    sikkerlogg.info("Svarer på isalive og isready")

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
        logg.info("En feil har oppstått:", exception)
        coroutineContext.cancel()
    }
    val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)
    scope.launch {
        sanityVarselendringerListener(iProduksjonsmiljø, sanityProjectId, sanityDataset, sender, bøtte)
    }
    val server = scope.embeddedServer(CIO, port = 8080) {
        routing {
            get("/isalive") { call.respondText("ALIVE!") }
            get("/isready") { call.respondText("READY!") }
        }
        monitor.subscribe(ApplicationStopped) {
            logg.info("Avslutter appen")
        }
    }
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1, 5, TimeUnit.SECONDS)
    })
    server.start(wait = true)
}
