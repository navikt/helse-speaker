package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

internal fun Routing.nais() {
    get("/internal/isready") {
        call.respond(HttpStatusCode.OK)
    }
    get("/internal/isalive") {
        call.respond(HttpStatusCode.OK)
    }
}