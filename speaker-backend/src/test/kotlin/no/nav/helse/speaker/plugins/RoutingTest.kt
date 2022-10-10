package no.nav.helse.speaker.plugins

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.helse.speaker.Varsel
import no.nav.helse.speaker.VarselRepository
import no.nav.helse.speaker.db.VarselException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class RoutingTest {
    private val varsel = Varsel("EN_VARSELKODE", "EN TITTEL", "EN FORKLARING", "EN HANDLING", false)

    @Test
    fun `hent varsler`() = testApplication {
        application {
            configureRestApi(repository())
            configureSerialization()
        }

        val response = client.get("/api/varsler")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(varsel), Json.decodeFromString<List<Varsel>>(response.body()))
    }

    @Test
    fun `oppdater varsel`() = testApplication {
        application {
            configureRestApi(repository())
            configureSerialization()
        }

        val response = client.post("/api/varsler/oppdater") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(varsel))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `oppdater varsel når kode ikke finnes`() = testApplication {
        application {
            configureRestApi(repository(shouldThrowOnUpdate = true))
            configureSerialization()
        }

        val response = client.post("/api/varsler/oppdater") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(Json.encodeToString(varsel))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    private fun repository(varsler: List<Varsel> = listOf(varsel), shouldThrowOnUpdate: Boolean = false): VarselRepository = object : VarselRepository {
        override fun nytt(varsel: Varsel): Boolean {
            TODO("Not yet implemented")
        }

        override fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean {
            TODO("Not yet implemented")
        }

        override fun finn(): List<Varsel> {
            return varsler
        }

        override fun oppdater(varsel: Varsel) {
            if (shouldThrowOnUpdate) throw VarselException.KodeFinnesIkke("EN_VARSELKODE")
        }

        override fun oppdater(kode: String, tittel: String, forklaring: String?, handling: String?, avviklet: Boolean) {
            TODO("Not yet implemented")
        }
    }

}