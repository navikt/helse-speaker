package no.nav.helse.speaker.routes

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.host
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import no.nav.helse.speaker.LocalDateTimeSerializer
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.plugins.InvalidSession
import no.nav.helse.speaker.plugins.SpeakerSession
import no.nav.helse.speaker.plugins.SpeakerSession.Companion.validerToken
import no.nav.helse.speaker.plugins.login
import no.nav.helse.speaker.redirectUrl
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.time.LocalDateTime

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal fun Application.speaker(varselRepository: VarselRepository, azureAD: AzureAD) {
    val logg = LoggerFactory.getLogger(Application::class.java)
    routing {
        authenticate("oauth") {
            login(azureAD)
            singlePageApplication {
                react("speaker-frontend/dist")
            }
            route("/api") {
                intercept(ApplicationCallPipeline.Setup) {
                    validerSesjon(azureAD)
                }
                get("/varsler") {
                    logg.info("Henter varsler")
                    call.respond(HttpStatusCode.OK, varselRepository.finn())
                }
                post("/varsler/oppdater") {
                    val varseldefinisjon = call.receive<Varseldefinisjon>()
                    sikkerlogg.info("Oppdaterer {}", varseldefinisjon)
                    try {
                        varselRepository.oppdater(varseldefinisjon)
                    } catch (e: VarselException) {
                        return@post call.respond(message = e.message!!, status = e.httpStatusCode)
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }
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




