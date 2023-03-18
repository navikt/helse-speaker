package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.speaker.Mediator
import no.nav.helse.speaker.domene.Bruker
import no.nav.helse.speaker.microsoft.AzureAD

internal fun Route.brukerRoutes(azureAD: AzureAD, mediator: Mediator) {
    get("/bruker") {
        val bruker = Bruker.fromCall(azureAD.issuer(), call)
        call.respond(HttpStatusCode.OK, bruker)
    }
    get("/teammedlemmer") {
        call.respond(HttpStatusCode.OK, mediator.finnTeammedlemmer())
    }
}

