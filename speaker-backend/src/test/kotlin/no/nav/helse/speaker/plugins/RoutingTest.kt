package no.nav.helse.speaker.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
                header("Authorization", "Bearer ${accessToken(scopes = listOf("openid", "offline_access", "$issuerId/.default", "other_scope"))}")
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
    fun `hent varsler uten gyldige scopes`() {
        val response = runBlocking {
            client.get("http://localhost:8080/api/varsler") {
                header("Authorization", "Bearer ${accessToken(scopes = listOf("openid", "offline_access"))}")
            }
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
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

    private fun accessToken(
        scopes: List<String> = listOf("openid", "offline_access", "$issuerId/.default"),
        harNavIdent: Boolean = true,
        grupper: List<String> = listOf("$groupId"),
        andreClaims: Map<String, String> = emptyMap()
    ): String {
        val claims = mutableMapOf(
            "scope" to scopes.joinToString(" "),
            "groups" to grupper
        ).apply {
            if (harNavIdent) put("NAVident", "EN_IDENT")
            putAll(andreClaims)
        }.toMap()
        return oauthMock.issueToken(audience = issuerId.toString(), claims = claims).serialize()
    }

    private companion object {
        private val varseldefinisjon =
            Varseldefinisjon("EN_VARSELKODE", "EN TITTEL", "EN FORKLARING", "EN HANDLING", false)
        private val oauthMock = MockOAuth2Server().also {
            it.start()
        }
        private val issuerId = UUID.randomUUID()
        private val groupId = UUID.randomUUID()

        private var shouldThrowOnUpdate: Boolean = false
        private fun setShouldThrowOnUpdate(): Boolean {
            return shouldThrowOnUpdate
        }

        private val env = mapOf(
            "AZURE_APP_WELL_KNOWN_URL" to oauthMock.wellKnownUrl("default").toString(),
            "AZURE_APP_CLIENT_ID" to "$issuerId",
            "AZURE_APP_CLIENT_SECRET" to "some_secret",
            "AUTHORIZATION_URL" to oauthMock.authorizationEndpointUrl("default").toString(),
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
