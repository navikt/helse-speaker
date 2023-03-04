package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal fun Route.speaker(varselRepository: VarselRepository) {
    val logg = LoggerFactory.getLogger(Application::class.java)
    get("/api/varsler") {
        logg.info("Henter varsler")
        sikkerlogg.info("Henter varsler")
        call.respond(HttpStatusCode.OK, varselRepository.finn())
    }
    post("/api/varsler/oppdater") {
        val varseldefinisjon = call.receive<Varseldefinisjon>()
        logg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
        sikkerlogg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
        try {
            varselRepository.oppdater(varseldefinisjon)
        } catch (e: VarselException) {
            return@post call.respond(message = e.message!!, status = e.httpStatusCode)
        }
        call.respond(HttpStatusCode.OK)
    }
}




