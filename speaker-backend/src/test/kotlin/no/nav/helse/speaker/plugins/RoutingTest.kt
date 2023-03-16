package no.nav.helse.speaker.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.speaker.Mediator
import no.nav.helse.speaker.app
import no.nav.helse.speaker.domene.Bruker
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime.now
import java.util.*

internal class RoutingTest {

    private lateinit var client: HttpClient

    @BeforeEach
    fun beforeEach() {
        client = HttpClient(CIO) {
            install(HttpCookies)
            configureClientContentNegotiation()
        }
        varselkodeFinnes = false
    }

    @Test
    fun `hent varsler med gyldig token`() = withTestApplication {
        val response = client.get("/api/varsler") {
            header("Authorization", "Bearer ${accessToken()}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<String>()
        assertEquals(listOf(varseldefinisjon()), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler med gyldig token med flere grupperider`() = withTestApplication {
        val response = client.get("/api/varsler") {
            header(
                "Authorization",
                "Bearer ${accessToken(grupper = listOf(groupId.toString(), "${UUID.randomUUID()}"))}"
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon()), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler med gyldig token med flere scopes`() = withTestApplication {
        val response = client.get("/api/varsler") {
            header("Authorization", "Bearer ${accessToken()}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon()), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler med gyldig token med andre claims i tillegg`() = withTestApplication {
        val response = client.get("/api/varsler") {
            header("Authorization", "Bearer ${accessToken(andreClaims = mapOf("Some other claim" to "some value"))}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.body<String>() }
        assertEquals(listOf(varseldefinisjon()), Json.decodeFromString<List<Varseldefinisjon>>(body))
    }

    @Test
    fun `hent varsler uten gyldige groups`() = withTestApplication {
        val response = client.get("/api/varsler") {
            header("Authorization", "Bearer ${accessToken(grupper = listOf("${UUID.randomUUID()}"))}")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `hent varsler uten NAVident`() = withTestApplication {
        val response = client.get("/api/varsler") {
            header("Authorization", "Bearer ${accessToken(harNavIdent = false)}")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Forventer definisjon-payload, ikke definisjon ved oppdatering`() = withTestApplication {
        varselkodeFinnes = true
        val response = client.post("/api/varsler/oppdater") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("Authorization", "Bearer ${accessToken()}")
            setBody(Json.encodeToString(varseldefinisjon()))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `oppdater varsel med gyldig token`() = withTestApplication {
        varselkodeFinnes = true
        val response = client.post("/api/varsler/oppdater") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("Authorization", "Bearer ${accessToken()}")
            setBody(Json.encodeToString(varseldefinisjonPayload(tittel = "NY TITTEL")))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `oppdater varsel når kode ikke finnes`() = withTestApplication {
        varselkodeFinnes = false

        val response = client.post("/api/varsler/oppdater") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("Authorization", "Bearer ${accessToken()}")
            setBody(Json.encodeToString(varseldefinisjonPayload()))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `får ikke hentet ny varselkode dersom man ikke er logget inn`() = withTestApplication {
        val response = client.get("/api/varsler/generer-kode") {
            parameter("subdomene", "SB")
            parameter("kontekst", "EX")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode`() = withTestApplication {
        val response = client.get("/api/varsler/generer-kode") {
            parameter("subdomene", "XX")
            parameter("kontekst", "YY")
            header("Authorization", "Bearer ${accessToken()}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("XX_YY_1", response.body<String>())
    }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved manglende subdomene`() =
        withTestApplication {
            val response = client.get("/api/varsler/generer-kode") {
                parameter("kontekst", "EX")
                header("Authorization", "Bearer ${accessToken()}")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved manglende kontekst`() =
        withTestApplication {
            val response = client.get("/api/varsler/generer-kode") {
                parameter("subdomene", "SB")
                header("Authorization", "Bearer ${accessToken()}")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved ugyldig subdomene`() =
        withTestApplication {
            val response = client.get("/api/varsler/generer-kode") {
                parameter("subdomene", "S")
                parameter("kontekst", "EX")
                header("Authorization", "Bearer ${accessToken()}")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `validering av subdomene og kontekst før genering av ny varselkode ved ugyldig kontekst`() =
        withTestApplication {
            val response = client.get("/api/varsler/generer-kode") {
                parameter("subdomene", "SB")
                parameter("kontekst", "E")
                header("Authorization", "Bearer ${accessToken()}")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `Henter bruker`() = withTestApplication {
        val userOid = UUID.randomUUID()
        val response = client.get("/api/bruker") {
            header("Authorization", "Bearer ${accessToken(oid = userOid)}")
        }
        val bruker = response.body<String>().let { Json.decodeFromString<Bruker>(it) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            Bruker(
                epostadresse = "some_username",
                navn = "some name",
                ident = "EN_IDENT",
                oid = userOid
            ),
            bruker
        )
    }

    @Test
    fun `Forsøker å hente bruker uten autentisering`() {
        withTestApplication {
            val response = client.get("/api/bruker")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `Henter ut subdomener og kontekster`() {
        withTestApplication {
            val response = client.get("/api/varsler/subdomener-og-kontekster") {
                header("Authorization", "Bearer ${accessToken()}")
            }
            val subdomenerOgKontekster = response.body<String>().let {
                Json.decodeFromString<Map<String, Set<String>>>(it)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                mapOf(
                    "AA" to setOf("BB", "CC")
                ),
                subdomenerOgKontekster
            )
        }
    }

    @Test
    fun `Forsøker å ut subdomener og kontekster uten autentisering`() {
        withTestApplication {
            val response = client.get("/api/varsler/subdomener-og-kontekster") {
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `Forventer definisjon-payload, ikke definisjon ved oppretting`() {
        varselkodeFinnes = false
        withTestApplication {
            val response = client.post("/api/varsler/opprett") {
                header("Authorization", "Bearer ${accessToken()}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(Json.encodeToString(varseldefinisjon()))
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `Lagrer ny definisjon`() {
        varselkodeFinnes = false
        withTestApplication {
            val response = client.post("/api/varsler/opprett") {
                header("Authorization", "Bearer ${accessToken()}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(Json.encodeToString(varseldefinisjonPayload()))
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `Forsøker å lagre nytt varsel uten autentisering`() {
        withTestApplication {
            val response = client.post("/api/varsler/opprett") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(Json.encodeToString(varseldefinisjonPayload()))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    private fun accessToken(
        harNavIdent: Boolean = true,
        grupper: List<String> = listOf("$groupId"),
        andreClaims: Map<String, String> = emptyMap(),
        oid: UUID = UUID.randomUUID()
    ): String {
        val claims: Map<String, Any> = mutableMapOf<String, Any>(
            "groups" to grupper
        ).apply {
            if (harNavIdent) putAll(
                mapOf(
                    "NAVident" to "EN_IDENT",
                    "preferred_username" to "some_username",
                    "name" to "some name",
                    "oid" to "$oid"
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

    private fun withTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            environment {
                module {
                    val repository = repository(varselkodeFinnes = ::settVarselkodeFinnes)
                    app(env, Mediator({ TestRapid() }, repository))
                }
            }
            block(this)
        }
    }

    private companion object {
        private fun varseldefinisjon(tittel: String = "EN TITTEL") =
            Varseldefinisjon(UUID.randomUUID(), "SB_EX_1", tittel, "EN FORKLARING", "EN HANDLING", false, emptyList(), now())
        private fun varseldefinisjonPayload(tittel: String = "EN TITTEL") =
            Varseldefinisjon.VarseldefinisjonPayload("SB_EX_1", tittel, "EN FORKLARING", "EN HANDLING", false, emptyList())
        private val oauthMock = MockOAuth2Server().also {
            it.start()
        }
        private val issuerId = UUID.randomUUID()
        private val groupId = UUID.randomUUID()
        private val clientId = UUID.randomUUID()

        private var varselkodeFinnes: Boolean = false
        private fun settVarselkodeFinnes(): Boolean {
            return varselkodeFinnes
        }

        private val env = mapOf(
            "AZURE_APP_WELL_KNOWN_URL" to oauthMock.wellKnownUrl(issuerId.toString()).toString(),
            "AZURE_APP_CLIENT_ID" to "$clientId",
            "LOCAL_DEVELOPMENT" to "true",
            "AZURE_VALID_GROUP_ID" to "$groupId"
        )

        private fun repository(
            varsler: List<Varseldefinisjon> = listOf(varseldefinisjon()),
            varselkodeFinnes: () -> Boolean = { true }
        ) = object : VarselRepository {
            override fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon> {
                return varsler
            }

            override fun finn(varselkode: String): Varselkode? {
                if (!varselkodeFinnes()) return null
                return Varselkode(varsler.first {it.kode() == varselkode})
            }

            override fun finnNesteVarselkodeFor(prefix: String): String {
                return "${prefix}_1"
            }

            override fun finnSubdomenerOgKontekster(): Map<String, Set<String>> {
                return mapOf(
                    "AA" to setOf("BB", "CC")
                )
            }
        }

        @BeforeAll
        @JvmStatic
        fun setupMock() {
        }

        @AfterAll
        @JvmStatic
        fun shutdownMock() {
            oauthMock.shutdown()
        }
    }
}
