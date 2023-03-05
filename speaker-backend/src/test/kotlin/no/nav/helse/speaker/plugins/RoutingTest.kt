package no.nav.helse.speaker.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.helse.speaker.app
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class RoutingTest {

    private lateinit var client: HttpClient

    @BeforeEach
    fun beforeEach() {
        client = HttpClient(CIO) {
            install(HttpCookies)
            configureClientContentNegotiation()
        }
        shouldThrowOnUpdate = false
    }

    @Test
    fun `hent varsler med gyldig token`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler med gyldig token med flere grupperider`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken(grupper = listOf(groupId.toString(), "${UUID.randomUUID()}"))}")
            }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler med gyldig token med flere scopes`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler med gyldig token med andre claims i tillegg`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken(andreClaims = mapOf("Some other claim" to "some value"))}")
            }
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler uten gyldige groups`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken(grupper = listOf("${UUID.randomUUID()}"))}")
            }
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `hent varsler uten NAVident`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken(harNavIdent = false)}")
            }
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `oppdater varsel med gyldig token`() {
        val response = runBlocking {
            client.post("http://localhost:8080/api/varsler/oppdater") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("Authorization", "Bearer ${accessToken()}")
                setBody(Json.encodeToString(varseldefinisjon))
            }
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `oppdater varsel når kode ikke finnes`() {
        shouldThrowOnUpdate = true

        val response = runBlocking {
            client.post("http://localhost:8080/api/varsler/oppdater") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("Authorization", "Bearer ${accessToken()}")
                setBody(Json.encodeToString(varseldefinisjon))
            }
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler/generer-kode") {
                parameter("subdomene", "SB")
                parameter("kontekst", "EX")
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved manglende subdomene`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler/generer-kode") {
                parameter("kontekst", "EX")
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved manglende kontekst`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler/generer-kode") {
                parameter("subdomene", "SB")
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved ugyldig subdomene`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler/generer-kode") {
                parameter("subdomene", "S")
                parameter("kontekst", "EX")
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved ugyldig kontekst`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler/generer-kode") {
                parameter("subdomene", "SB")
                parameter("kontekst", "E")
                header("Authorization", "Bearer ${accessToken()}")
            }
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private fun accessToken(
        harNavIdent: Boolean = true,
        grupper: List<String> = listOf("$groupId"),
        andreClaims: Map<String, String> = emptyMap()
    ): String {
        val claims: Map<String, Any> = mutableMapOf<String, Any>(
            "groups" to grupper
        ).apply {
            if (harNavIdent) putAll(
                mapOf(
                    "NAVident" to "EN_IDENT",
                    "preferred_username" to "some_username",
                    "name" to "some name"
                )
            )
            putAll(andreClaims)
        }
        return oauthMock.issueToken(
            issuerId = issuerId.toString(),
            clientId = clientId.toString(),
            tokenCallback = DefaultOAuth2TokenCallback(
                issuerId = issuerId.toString(),
                audience = listOf(clientId.toString()),
                claims = claims,
            )
        ).serialize()
    }

    private companion object {
        private val varseldefinisjon =
            Varseldefinisjon("EN_VARSELKODE", "EN TITTEL", "EN FORKLARING", "EN HANDLING", false)
        private val oauthMock = MockOAuth2Server().also {
            it.start()
        }
        private val issuerId = UUID.randomUUID()
        private val groupId = UUID.randomUUID()
        private val clientId = UUID.randomUUID()

        private var shouldThrowOnUpdate: Boolean = false
        private fun setShouldThrowOnUpdate(): Boolean {
            return shouldThrowOnUpdate
        }

        private val env = mapOf(
            "AZURE_APP_WELL_KNOWN_URL" to oauthMock.wellKnownUrl(issuerId.toString()).toString(),
            "AZURE_APP_CLIENT_ID" to "$clientId",
            "LOCAL_DEVELOPMENT" to "true",
            "AZURE_VALID_GROUP_ID" to "$groupId"
        )

        private val server: ApplicationEngine = embeddedServer(
            Netty,
            port = 8080,
            host = "localhost",
            module = { app(repository(shouldThrowOnUpdate = ::setShouldThrowOnUpdate), env) }
        )

        private fun repository(
            varsler: List<Varseldefinisjon> = listOf(varseldefinisjon),
            shouldThrowOnUpdate: () -> Boolean = { false }
        ) = object : VarselRepository {
            override fun nytt(varseldefinisjon: Varseldefinisjon): Boolean {
                TODO("Not yet implemented")
            }

            override fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun finn(): List<Varseldefinisjon> {
                return varsler
            }

            override fun finnGjeldendeDefinisjonFor(varselkode: String): Varseldefinisjon {
                TODO("Not yet implemented")
            }

            override fun oppdater(varseldefinisjon: Varseldefinisjon) {
                if (shouldThrowOnUpdate()) throw VarselException.KodeFinnesIkke("EN_VARSELKODE")
            }

            override fun finnNesteVarselkodeFor(prefix: String): String {
                return "${prefix}_1"
            }
        }

        @BeforeAll
        @JvmStatic
        fun setupMock() {
            server.start(wait = false)
        }

        @AfterAll
        @JvmStatic
        fun shutdownMock() {
            oauthMock.shutdown()
            server.stop(100, 100)
        }
    }
}
