package no.nav.helse.speaker.plugins

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.sessions.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.redirectUrl
import org.slf4j.LoggerFactory

internal fun Route.login(azureAD: AzureAD) {
    val logg = LoggerFactory.getLogger("tjenestekall")
    get("/login") { /* Redirects to 'authorizeUrl' automatically*/ }
    get("/oauth2/callback") {
        val principal = call.principal<OAuth2>() ?: throw IllegalStateException("Forventer å finne token")
        val accessToken = principal.accessToken
        val refreshToken = principal.refreshToken ?: throw IllegalStateException("Forventer å finne refreshToken")
        val idToken = principal.extraParameters["id_token"] ?: throw IllegalStateException("Forventer å finne idToken")
        val state = principal.state ?: throw IllegalStateException("Forventer å finne state")
        val expiry = principal.expiresIn
        val redirectUrl = azureAD.getRedirect(state)
        logg.info("Innlogging vellykket")
        call.sessions.set(SpeakerSession(accessToken, refreshToken, idToken, expiry))
        redirectUrl?.also { url ->
            logg.info("Omdirigerer til $url")
            call.respondRedirect(url)
        }
        call.respondRedirect("/")
    }
}

internal fun Application.configureSessions(isDevelopment: Boolean) {
    install(Sessions) {
        cookie<SpeakerSession>("speaker", storage = SessionStorageMemory()) {
            this.cookie.secure = !isDevelopment
            cookie.extensions["SameSite"] = "strict"
            serializer = object : SessionSerializer<SpeakerSession> {
                override fun deserialize(text: String): SpeakerSession {
                    return Json.decodeFromString(text)
                }
                override fun serialize(session: SpeakerSession): String {
                    return Json.encodeToString(session)
                }
            }
        }
    }
}


internal fun Application.configureAuthentication(azureAD: AzureAD, isDevelopment: Boolean) {
    install(Authentication) {
        oauth("oauth") {
            urlProvider = {
                redirectUrl("/oauth2/callback", isDevelopment)
            }
            skipWhen { call ->
                call.sessions.get<SpeakerSession>() != null
            }

            providerLookup = {
                azureAD.oauthSettings()
            }
            client = HttpClient(Apache) {
                configureClientContentNegotiation()
            }
        }
    }
}