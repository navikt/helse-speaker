package no.nav.helse.speaker.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {

    // Starting point for a Ktor app:
    routing {
        get("/varsler") {
            call.respondText("Hello World!")
        }
    }
}
