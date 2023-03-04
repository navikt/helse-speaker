package no.nav.helse.speaker.azure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.server.auth.OAuthServerSettings
import kotlinx.serialization.json.JsonObject
import no.nav.helse.speaker.plugins.SpeakerSession
import no.nav.helse.speaker.plugins.configureClientContentNegotiation
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class AzureAD private constructor(private val config: Config) {
    private val redirects = mutableMapOf<String, String>()
    private val httpClient = HttpClient(Apache) {
        configureClientContentNegotiation()
    }

    internal fun getRedirect(state: String): String? = redirects[state]

    internal fun oauthSettings(): OAuthServerSettings.OAuth2ServerSettings {
        return OAuthServerSettings.OAuth2ServerSettings(
            name = "AAD",
            authorizeUrl = config.authorizationEndpoint,
            accessTokenUrl = config.tokenEndpoint,
            requestMethod = HttpMethod.Post,
            clientId = config.clientId,
            clientSecret = config.clientSecret,
            defaultScopes = listOf("openid", "offline_access", "${config.clientId}/.default"),
            onStateCreated = { call, state ->
                call.request.queryParameters["redirectUrl"]?.also {
                    redirects[state] = it
                }
            }
        )
    }

    internal suspend fun fornyToken(token: String): SpeakerSession {
        val requestBody = listOf(
            "tenant" to "nav.no",
            "client_id" to config.clientId,
            "grant_type" to "refresh_token",
            "scope" to "openid offline_access ${config.clientId}/.default",
            "refresh_token" to token,
            "client_secret" to config.clientSecret,
        ).formUrlEncode()
        val response = httpClient.post(config.tokenEndpoint) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(requestBody)
        }

        val jsonRespons = response.body<JsonObject>()
        return SpeakerSession(
            accessToken = jsonRespons["access_token"].toString(),
            refreshToken = jsonRespons["refresh_token"].toString(),
            idToken = jsonRespons["id_token"].toString(),
            expiresIn = jsonRespons["expires_in"].toString().toLong()
        )
    }

    internal companion object {
        internal fun fromEnv(env: Map<String, String>): AzureAD {
            return AzureAD(
                Config(
                    discoveryUrl = env.getValue("AZURE_APP_WELL_KNOWN_URL"),
                    clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                    clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
                    authorizationUrl = env["AUTHORIZATION_URL"]
                )
            )
        }
    }

    private class Config(
        internal val discoveryUrl: String,
        internal val clientId: String,
        internal val clientSecret: String,
        authorizationUrl: String?
    ) {
        private val discovered = discoveryUrl.discover()
        internal val tokenEndpoint = discovered["token_endpoint"]?.textValue()
            ?: throw RuntimeException("Unable to discover token endpoint")

        internal val authorizationEndpoint = authorizationUrl ?: discovered["authorization_endpoint"]?.textValue()
        ?: throw RuntimeException("Unable to discover authorization endpoint")

        private companion object {
            private fun String.discover(): JsonNode {
                val (responseCode, responseBody) = this.fetchUrl()
                if (responseCode >= 300 || responseBody == null) throw IOException("got status $responseCode from ${this}.")
                return jacksonObjectMapper().readTree(responseBody)
            }

            private fun String.fetchUrl() = with(URL(this).openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
                responseCode to stream?.bufferedReader()?.readText()
            }
        }
    }
}

