package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

internal fun Application.nais() {
    routing {
        get("/isready") {
            call.respond(HttpStatusCode.OK)
        }
        get("/isalive") {
            call.respond(HttpStatusCode.OK)
        }
    }
}