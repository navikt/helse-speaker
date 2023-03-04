package no.nav.helse.speaker.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import no.nav.helse.speaker.Feilrespons
import java.util.*

class InvalidSession(message: String) : RuntimeException(message)

internal fun Application.statusPages() {
    install(StatusPages) {
        suspend fun respondToException(
            status: HttpStatusCode,
            call: ApplicationCall,
            cause: Throwable
        ) {
            val errorId = UUID.randomUUID()
            call.respond(status, Feilrespons(errorId.toString(), cause.message))
        }
        exception<NotFoundException> { call, cause ->
            respondToException(HttpStatusCode.NotFound, call, cause)
        }
        exception<BadRequestException> { call, cause ->
            respondToException(HttpStatusCode.BadRequest, call, cause)
        }
        exception<InvalidSession> { call, cause ->
            call.sessions.clear<SpeakerSession>()
            respondToException(HttpStatusCode.Unauthorized, call, cause)
        }
        exception<Throwable> { call, cause ->
            respondToException(HttpStatusCode.InternalServerError, call, cause)
        }
    }
}