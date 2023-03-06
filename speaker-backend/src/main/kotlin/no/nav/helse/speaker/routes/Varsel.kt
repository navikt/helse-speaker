package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import org.slf4j.LoggerFactory

internal fun Route.varselRoutes(varselRepository: VarselRepository) {
    val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    val logg = LoggerFactory.getLogger(Route::class.java)

    route("/varsler") {
        get {
            logg.info("Henter varsler")
            sikkerlogg.info("Henter varsler")
            call.respond(HttpStatusCode.OK, varselRepository.finn())
        }
        post("/oppdater") {
            val varseldefinisjon = call.receive<Varseldefinisjon>()
            logg.info("Oppdaterer {}", StructuredArguments.kv("varselkode", varseldefinisjon.kode()))
            sikkerlogg.info("Oppdaterer {}", StructuredArguments.kv("varselkode", varseldefinisjon.kode()))
            try {
                varselRepository.oppdater(varseldefinisjon)
            } catch (e: VarselException) {
                return@post call.respond(message = e.message!!, status = e.httpStatusCode)
            }
            call.respond(HttpStatusCode.OK)
        }
        get("/generer-kode") {
            val mønsterSomMåMatche = "^\\D{2}$".toRegex()
            val subdomene = call.request.queryParameters["subdomene"]
            val kontekst = call.request.queryParameters["kontekst"]

            if (subdomene == null)
                return@get call.respond(HttpStatusCode.BadRequest, "Mangler subdomene")

            if (!subdomene.matches(mønsterSomMåMatche))
                return@get call.respond(HttpStatusCode.BadRequest, "Subdomene er ikke på forventet format ${mønsterSomMåMatche.pattern}")

            if (kontekst == null)
                return@get call.respond(HttpStatusCode.BadRequest, "Mangler kontekst")

            if (!kontekst.matches(mønsterSomMåMatche))
                return@get call.respond(HttpStatusCode.BadRequest, "Kontekst er ikke på forventet format ${mønsterSomMåMatche.pattern}")

            val prefix = "${subdomene}_${kontekst}"
            val nesteVarselkode = varselRepository.finnNesteVarselkodeFor(prefix)
            call.respond(HttpStatusCode.OK, nesteVarselkode)
        }
        get("/subdomener-og-kontekster") {
            call.respond(HttpStatusCode.OK, varselRepository.finnSubdomenerOgKontekster())
        }
    }

}