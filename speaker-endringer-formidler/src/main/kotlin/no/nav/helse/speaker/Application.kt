package no.nav.helse.speaker

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

internal val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
internal val logg = LoggerFactory.getLogger("SpeakerEndringerFormidler")

internal val objectMapper =
    jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun main() {
    val env = System.getenv()
    val sender = Kafka
    app(env, sender)
}

fun app(
    env: Map<String, String>,
    sender: Sender,
) {
    val sanityProjectId = env["SANITY_PROJECT_ID"] ?: throw IllegalArgumentException("Mangler sanity projectId")
    val sanityDataset = env["SANITY_DATASET"] ?: throw IllegalArgumentException("Mangler sanity dataset")
    val iProduksjonsmiljø = env["NAIS_CLUSTER_NAME"] == "prod-gcp"
    val bøttenavn = env["BUCKET_NAME"] ?: throw IllegalArgumentException("Mangler bucket name")

    val scope = CoroutineScope(Job())
    scope.launch {
        logg.info("Etablerer lytter mot Sanity")
        sikkerlogg.info("Etablerer lytter mot Sanity")
        sanityVarselendringerListener(iProduksjonsmiljø, sanityProjectId, sanityDataset, sender, GCPBøtte(bøttenavn))
    }
    scope.launch {
        logg.info("Svarer på isalive og isready")
        sikkerlogg.info("Svarer på isalive og isready")
        embeddedServer(CIO, port = 8080) {
            routing {
                get("/isalive") { call.respondText("ALIVE!") }
                get("/isready") { call.respondText("READY!") }
            }
        }.start(wait = true)
    }
}
