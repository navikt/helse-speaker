package no.nav.helse.speaker.microsoft

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.jwk.RSAKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import no.nav.security.token.support.v2.IssuerConfig
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.*


class AzureAD private constructor(private val config: Config) {
    private val httpClient: HttpClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json()
        }
    }

    internal fun issuer(): String = "AAD"
    internal fun issuerConfig(): IssuerConfig = IssuerConfig(
        name = issuer(),
        discoveryUrl = config.discoveryUrl,
        acceptedAudience = listOf(config.clientId),
    )
    private val requiredClaims = mapOf(
        "NAVident" to null,
        "preferred_username" to null,
        "name" to null,
        "azp" to config.clientId,
        "aud" to listOf(config.clientId)
    )
    private val requiredGroups = listOf(config.validGroupId)

    internal fun hasValidClaimValues(claimsAndValues: Map<String, Any>) =
        claimsAndValues.entries.fold(true) { _, (key, value) ->
            if (requiredClaims[key] == value) return@fold true
            false
        }

    internal fun hasValidClaims(claims: List<String>) = requiredClaims.keys.all { it in claims }
    internal fun hasValidGroups(groups: List<String>) = requiredGroups.any { it in groups }

    internal suspend fun fetchToken(): AadAccessToken {
        val privateKey = RSAKey.parse(config.privateJwk)
        val now = Instant.now()
        val clientAssertion = JWT.create().apply {
            withKeyId(privateKey.keyID)
            withSubject(config.clientId)
            withIssuer(config.clientId)
            withAudience(config.tokenEndpoint)
            withJWTId(UUID.randomUUID().toString())
            withIssuedAt(Date.from(now))
            withNotBefore(Date.from(now))
            withExpiresAt(Date.from(now.plusSeconds(120)))
        }.sign(Algorithm.RSA256(null, privateKey.toRSAPrivateKey()))

        val callId = UUID.randomUUID()
        logg.info("Henter Azure token for MS graph")
        val token = httpClient.post(config.tokenEndpoint) {
            header("callId", callId)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(FormDataContent(Parameters.build {
                append("grant_type", "client_credentials")
                append("scope", "https://graph.microsoft.com/.default")
                append("client_id", config.clientId)
                append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                append("client_assertion", clientAssertion)
            }))
        }.body<AadAccessToken>()

        logg.info("hentet token for MS graph")

        return token
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class AadAccessToken(val access_token: String)

    internal companion object {
        private val logg = LoggerFactory.getLogger(AzureAD::class.java)

        internal fun fromEnv(env: Map<String, String>): AzureAD {
            return AzureAD(
                Config(
                    discoveryUrl = env.getValue("AZURE_APP_WELL_KNOWN_URL"),
                    clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                    validGroupId = env.getValue("AZURE_VALID_GROUP_ID"),
                    privateJwk = env.getValue("AZURE_APP_JWK")
                )
            )
        }
    }

    private class Config(
        internal val discoveryUrl: String,
        internal val clientId: String,
        internal val validGroupId: String,
        internal val privateJwk: String
    ) {
        private val discovered = discoveryUrl.discover()
        internal val tokenEndpoint = discovered["token_endpoint"]?.textValue() ?: throw RuntimeException("Unable to discover token endpoint")

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

