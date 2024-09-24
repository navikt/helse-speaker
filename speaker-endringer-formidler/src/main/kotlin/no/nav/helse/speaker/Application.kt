package no.nav.helse.speaker

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

internal val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
internal val logg = LoggerFactory.getLogger("SpeakerEndringerFormidler")

internal val objectMapper =
    jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

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
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        logg.info("En feil har oppstått:", exception)
        exitProcess(1)
    }
    val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)
    scope.launch {
        sanityVarselendringerListener(iProduksjonsmiljø, sanityProjectId, sanityDataset, sender, bøtte)
    }
    scope.embeddedServer(CIO, port = 8080) {
        routing {
            get("/isalive") { call.respondText("ALIVE!") }
            get("/isready") { call.respondText("READY!") }
        }
    }.start(wait = true)
}
