package no.nav.helse.speaker.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import no.nav.helse.speaker.VarselRepository

internal fun Application.configureRouting(varselRepository: VarselRepository) {

    // Starting point for a Ktor app:
    routing {
        get("/api/varsler") {
            call.respond(HttpStatusCode.OK, varselRepository.finn())
        }
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
