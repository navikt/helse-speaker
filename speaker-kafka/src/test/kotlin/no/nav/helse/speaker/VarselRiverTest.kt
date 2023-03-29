package no.nav.helse.speaker

import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VarselRiverTest {

    private val rapid = TestRapid()
    private val repository = mockk<VarselRepository>(relaxed = true)

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        VarselRiver(rapid, repository)
    }

    @Test
    fun `leser aktiviteter`() {
        val melding = lesMelding("testmelding.json")
        rapid.sendTestMessage(melding)
        verify(exactly = 1) { repository.varselkoderFor(any()) }
    }

    @Test
    fun `aktivitetslogg uten aktiviteter leses ikke inn`() {
        val melding = lesMelding("testmelding_uten_aktiviteter.json")
        rapid.sendTestMessage(melding)
        verify(exactly = 0) { repository.varselkoderFor(any()) }
    }

    @Test
    fun `aktivitetslogg uten fødselsnummer leses ikke inn`() {
        val melding = lesMelding("testmelding_uten_fnr.json")
        rapid.sendTestMessage(melding)
        verify(exactly = 0) { repository.varselkoderFor(any()) }
    }

    private fun lesMelding(filnavn: String) =
        VarselRiverTest::class.java.classLoader.getResource("meldinger/$filnavn")!!.readText()
}