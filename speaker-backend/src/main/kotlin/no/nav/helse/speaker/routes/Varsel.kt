package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.slf4j.LoggerFactory

internal fun Route.varselRoutes(varselRepository: VarselRepository) {
    val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    val logg = LoggerFactory.getLogger(Route::class.java)

    route("/varsler") {
        get {
            logg.info("Henter varsler")
            sikkerlogg.info("Henter varsler")
            call.respond(HttpStatusCode.OK, varselRepository.finnGjeldendeDefinisjoner())
        }
        post("/oppdater") {
            val varseldefinisjon = call.receive<Varseldefinisjon>()
            val varselkode = varselRepository.finn(varseldefinisjon.kode())
                ?: return@post call.respond(HttpStatusCode.NotFound, message = "Finner ikke varselkode")
            logg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
            sikkerlogg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
            try {
                varselkode.håndter(varseldefinisjon)
                varselRepository.oppdater(varselkode)
            } catch (e: VarselException) {
                return@post call.respond(message = e.message!!, status = e.httpStatusCode)
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/opprett") {
            val varseldefinisjon = call.receive<Varseldefinisjon>()
            if (varselRepository.finn(varseldefinisjon.kode()) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Varselkode finnes allerede")
            }
            logg.info("Oppretter {}", kv("varselkode", varseldefinisjon.kode()))
            sikkerlogg.info("Oppretter {}", kv("varselkode", varseldefinisjon.kode()))
            val varselkode = Varselkode(varseldefinisjon)
            try {
                varselRepository.opprett(varselkode)
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