package no.nav.helse.speaker.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import no.nav.helse.speaker.Varsel
import no.nav.helse.speaker.VarselRepository
import no.nav.helse.speaker.db.VarselException
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal fun Application.configureRestApi(varselRepository: VarselRepository) {

    // Starting point for a Ktor app:
    routing {
        get("/api/varsler") {
            call.respond(HttpStatusCode.OK, varselRepository.finn())
        }
        post("/api/varsler/oppdater") {
            val varsel = call.receive<Varsel>()
            sikkerlogg.info("Oppdaterer {}", varsel)
            try {
                varselRepository.oppdater(varsel)
            } catch (e: VarselException) {
                return@post call.respond(message = e.message!!, status = e.httpStatusCode)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}

internal fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
