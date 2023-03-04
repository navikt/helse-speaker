package no.nav.helse.speaker.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.plugins.InvalidSession
import no.nav.helse.speaker.plugins.SpeakerSession
import no.nav.helse.speaker.plugins.SpeakerSession.Companion.validerToken
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal fun Route.speaker(varselRepository: VarselRepository, azureAD: AzureAD) {
    val logg = LoggerFactory.getLogger(Application::class.java)
    route("/api") {
        intercept(ApplicationCallPipeline.Setup) {
            validerSesjon(azureAD)
        }
        get("/varsler") {
            logg.info("Henter varsler")
            sikkerlogg.info("Henter varsler")
            call.respond(HttpStatusCode.OK, varselRepository.finn())
        }
        post("/varsler/oppdater") {
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
}

private suspend fun PipelineContext<Unit, ApplicationCall>.validerSesjon(azureAD: AzureAD) {
    val logg = LoggerFactory.getLogger(Application::class.java)
    logg.info("Intercepter kall til ${call.request.uri} med metode=${call.request.httpMethod.value}")
    logg.info("Ser etter sesjon")

    val session = call.sessions.get<SpeakerSession>() ?: throw InvalidSession("Forventer å finne sesjon")

    logg.info("Har sesjon, validerer gyldighet for token")
    validerToken(azureAD, session)
}




