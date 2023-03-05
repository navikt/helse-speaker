package no.nav.helse.speaker.domene

import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VarselkodeTest {

    private val varselkoder = listOf(
        Varselkode("AA_BB_1"),
        Varselkode("MM_NN_1"),
    )

    @Test
    fun `kan generere neste i rekken`() {
        val kode = varselkoder.finnNeste("AA_BB")
        assertEquals("AA_BB_2", kode)
    }

    @Test
    fun `genererer første i rekken dersom prefix ikke finnes fra før av`() {
        val kode = varselkoder.finnNeste("XX_YY")
        assertEquals("XX_YY_1", kode)
    }

    @Test
    fun `må ha både domene og kontekst lik eksisterende for å få neste`() {
        val kode1 = varselkoder.finnNeste("MM_OO")
        val kode2 = varselkoder.finnNeste("LL_NN")
        assertEquals("MM_OO_1", kode1)
        assertEquals("LL_NN_1", kode2)
    }

    @Test
    fun `gyldige varselkoder`() {
        assertDoesNotThrow {
            Varselkode("SB_RV_1")
        }
        assertDoesNotThrow {
            Varselkode("SB_RV_12")
        }
        assertDoesNotThrow {
            Varselkode("SB_RV_123")
        }
    }

    @Test
    fun `ugyldige varselkoder`() {
        assertThrows<IllegalArgumentException> {
            Varselkode("EN_SB_RV_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("noe SB_RV_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RV_1_EN")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RV_1 noe")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SBW_RV_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RVW_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB1_RV_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RV1_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RV_A1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RV_1A")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SBRV_1")
        }
        assertThrows<IllegalArgumentException> {
            Varselkode("SB_RV1")
        }
    }
}