package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AktivitetRiverTest {

    private val rapid = TestRapid()

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        AktivitetRiver(rapid)
    }

    @Test
    fun `leser aktiviteter`() {
        val melding = lesMelding("testmelding.json")
        rapid.sendTestMessage(melding)
        assertEquals(1, rapid.inspektør.size)
    }

    @Test
    fun innhold() {
        val melding = lesMelding("testmelding.json")
        rapid.sendTestMessage(melding)
        assertEquals("aktivitetslogg_nye_varsler", rapid.inspektør.message(0)["@event_name"].asText())
        assertEquals("12345678910", rapid.inspektør.message(0)["fødselsnummer"].asText())
        assertEquals(1, rapid.inspektør.message(0)["varsler"].size())
    }

    @Test
    fun `aktiviteter uten varsler medfører ikke melding på Kafka`() {
        val melding = lesMelding("testmelding_uten_varsler.json")
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


    private fun lesMelding(filnavn: String) =
        AktivitetRiverTest::class.java.classLoader.getResource("meldinger/$filnavn")!!.readText()
}