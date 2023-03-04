package no.nav.helse.speaker.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import no.nav.helse.speaker.LocalDateTimeSerializer
import no.nav.helse.speaker.azure.AzureAD
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Serializable
class SpeakerSession private constructor(
    private val accessToken: String,
    private val refreshToken: String,
    private val idToken: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    private val gyldigTil: LocalDateTime
) {
    internal constructor(
        accessToken: String,
        refreshToken: String,
        idToken: String,
        expiresIn: Long
    ): this(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        gyldigTil = LocalDateTime.now().plusSeconds(expiresIn)
    )

    private fun erGyldig() = gyldigTil > LocalDateTime.now().plusMinutes(1) // Legg på litt buffer

    internal companion object {
        internal suspend fun PipelineContext<Unit, ApplicationCall>.validerToken(azureAD: AzureAD, session: SpeakerSession) {
            val logg = LoggerFactory.getLogger("tjenestekall")
            if (session.erGyldig()) {
                logg.info("Sesjon er gyldig, behøver ikke hente nytt token")
                return
            }
            logg.info("Sesjon er ikke lenger gyldig, forsøker å fornye token")
            val nyttToken = azureAD.fornyToken(session.refreshToken)
            logg.info("Token fornyet, oppdaterer sesjon")
            call.sessions.set(nyttToken)
        }
    }
}