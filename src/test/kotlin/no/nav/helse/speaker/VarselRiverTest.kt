package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime
import java.util.*

internal class VarselRiverTest {

    private val rapid = TestRapid()

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        VarselRiver(rapid, repo)
    }

    @Test
    fun `leser aktiviteter`() {
        val melding = lesMelding("testmelding.json")
        rapid.sendTestMessage(melding)
        assertEquals(1, rapid.inspektør.size)
    }

    @Test
    fun innhold() {
        val melding = lesMelding("testmelding_innhold.json")
        rapid.sendTestMessage(melding)
        val varsler = rapid.inspektør.message(0)["varsler"]
        val varsel = varsler[0]
        assertEquals("aktivitetslogg_nye_varsler", rapid.inspektør.message(0)["@event_name"].asText())
        assertEquals("12345678910", rapid.inspektør.message(0)["fødselsnummer"].asText())
        assertEquals(1, varsler.size())
        assertEquals("A_BC_1", varsel["kode"].asText())
        assertEquals("En melding", varsel["tittel"].asText())
        assertDoesNotThrow { LocalDateTime.parse(varsel["tidsstempel"].asText()) }
        assertDoesNotThrow { UUID.fromString(varsel["id"].asText()) }
        assertEquals(2, varsel["kontekster"].size())
    }

    @Test
    fun `aktiviteter uten varsler medfører ikke melding på Kafka`() {
        val melding = lesMelding("testmelding_uten_varsler.json")
        rapid.sendTestMessage(melding)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `varsel uten varselkode medfører ikke melding på kafka`() {
        val melding = lesMelding("testmelding_varsel_uten_varselkode.json")
        rapid.sendTestMessage(melding)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `varsel uten id medfører ikke melding på kafka`() {
        val melding = lesMelding("testmelding_varsel_uten_id.json")
        rapid.sendTestMessage(melding)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `aktivitetslogg uten aktiviteter leses ikke inn`() {
        val melding = lesMelding("testmelding_uten_aktiviteter.json")
        rapid.sendTestMessage(melding)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `aktivitetslogg uten fødselsnummer leses ikke inn`() {
        val melding = lesMelding("testmelding_uten_fnr.json")
        rapid.sendTestMessage(melding)
        assertEquals(0, rapid.inspektør.size)
    }

    private val repo = object : VarselRepository {
        override fun opprett(
            id: UUID,
            kode: String,
            melding: String,
            kontekster: List<JsonNode>,
            tidsstempel: LocalDateTime
        ): Varsel {
            return Varsel.gyldig(id, melding, kode, kontekster, tidsstempel, null, null, false)
        }
    }


    private fun lesMelding(filnavn: String) =
        VarselRiverTest::class.java.classLoader.getResource("meldinger/$filnavn")!!.readText()
}