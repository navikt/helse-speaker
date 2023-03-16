package no.nav.helse.speaker.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.nav.helse.speaker.Mediator
import no.nav.helse.speaker.azure.AzureAD


internal fun Route.speaker(azureAD: AzureAD, mediator: Mediator) {
    route("/api") {
        varselRoutes(mediator)
        brukerRoutes(azureAD)
    }
}




