package no.nav.helse.speaker.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.nav.helse.speaker.Mediator


internal fun Route.speaker(identityIssuer: String, mediator: Mediator) {
    route("/api") {
        varselRoutes(mediator)
        brukerRoutes(identityIssuer, mediator)
    }
}




