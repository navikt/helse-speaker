package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.speaker.db.AbstractDatabaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime
import java.util.*

internal class VarselE2ETest: AbstractDatabaseTest() {
    private val rapid = TestRapid()

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        VarselRiver(rapid, ActualVarselRepository { dataSource })
    }

    @Test
    fun `database supplerer varsel med forklaring, handling og avviklet`() {
        opprettVarsel("A_BC_1")
        rapid.sendTestMessage(lesMelding("testmelding.json"))

        val varsler = rapid.inspektør.message(0)["varsler"]
        val varsel = varsler[0]
        assertEquals(1, varsler.size())
        assertEquals("A_BC_1", varsel["kode"].asText())
        assertEquals("EN TITTEL", varsel["tittel"].asText())
        assertEquals("EN FORKLARING", varsel["forklaring"].asText())
        assertEquals("EN HANDLING", varsel["handling"].asText())
        assertFalse(varsel["avviklet"].asBoolean())
        assertDoesNotThrow { LocalDateTime.parse(varsel["tidsstempel"].asText()) }
        assertDoesNotThrow { UUID.fromString(varsel["id"].asText()) }
        assertEquals(5, varsel["kontekster"].size())
    }

    private fun lesMelding(filnavn: String) =
        VarselRiverTest::class.java.classLoader.getResource("meldinger/$filnavn")!!.readText()
}