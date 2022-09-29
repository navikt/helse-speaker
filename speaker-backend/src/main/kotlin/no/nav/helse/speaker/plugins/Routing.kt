package no.nav.helse.speaker.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    // Starting point for a Ktor app:
    routing {
        get("/varsler") {
            call.respondText("Hello World!")
        }
    }
    routing {
    }
}
