package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import no.nav.helse.speaker.microsoft.AzureAD
import no.nav.helse.speaker.domene.Bruker

internal fun Route.brukerRoutes(azureAD: AzureAD) {
    route("/bruker") {
        get {
            val bruker = Bruker.fromCall(azureAD.issuer(), call)
            call.respond(HttpStatusCode.OK, bruker)
        }
    }
}

