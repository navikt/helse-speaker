package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.helse.speaker.Mediator
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.Varseldefinisjon

internal fun Route.varselRoutes(mediator: Mediator) {
    route("/varsler") {
        get {
            call.respond(HttpStatusCode.OK, mediator.finnGjeldendeVarseldefinisjoner())
        }
        post("/oppdater") {
            val varseldefinisjon = call.receive<Varseldefinisjon.VarseldefinisjonPayload>().toVarseldefinisjon()
            try {
                mediator.håndterOppdatertVarselkode(varseldefinisjon)
            } catch (e: VarselException) {
                return@post call.respond(message = e.message!!, status = e.httpStatusCode)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/opprett") {
            val varseldefinisjon = call.receive<Varseldefinisjon.VarseldefinisjonPayload>().toVarseldefinisjon()
            try {
                mediator.håndterNyVarselkode(varseldefinisjon)
            } catch (e: VarselException) {
                return@post call.respond(message = e.message!!, status = e.httpStatusCode)
            }
            call.respond(HttpStatusCode.OK)
        }
        get("/generer-kode") {
            val subdomene = call.request.queryParameters["subdomene"]
            val kontekst = call.request.queryParameters["kontekst"]
            val nesteVarselkode = try {
                mediator.finnNesteVarselkode(subdomene, kontekst)
            } catch (e: VarselException) {
                return@get call.respond(message = e.message!!, status = e.httpStatusCode)
            }
            call.respond(HttpStatusCode.OK, nesteVarselkode)
        }
        get("/subdomener-og-kontekster") {
            call.respond(HttpStatusCode.OK, mediator.finnSubdomenerOgKontekster())
        }
    }

}