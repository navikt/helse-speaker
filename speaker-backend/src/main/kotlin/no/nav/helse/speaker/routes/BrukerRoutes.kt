package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.helse.speaker.Mediator
import no.nav.helse.speaker.domene.Bruker
import no.nav.helse.speaker.domene.BrukerException
import no.nav.helse.speaker.domene.TeammedlemmerException

internal fun Route.brukerRoutes(identityIssuer: String, mediator: Mediator) {
    get("/bruker") {
        val bruker = try {
            Bruker.fromCall(identityIssuer, call)
        } catch (e: BrukerException) {
            return@get call.respond(e.httpStatusCode, e.message!!)
        }
        call.respond(HttpStatusCode.OK, bruker)
    }
    get("/teammedlemmer") {
        val teammedlemmer = try {
            mediator.finnTeammedlemmer()
        } catch (e: TeammedlemmerException) {
            return@get call.respond(e.httpStatusCode, e.message!!)
        }
        call.respond(HttpStatusCode.OK, teammedlemmer)
    }
}

