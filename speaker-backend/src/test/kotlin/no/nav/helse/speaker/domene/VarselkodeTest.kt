package no.nav.helse.speaker.domene

import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VarselkodeTest {

    private val varselkoder = setOf(
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
        assertUgyldigVarselkode("EN_SB_RV_1")
        assertUgyldigVarselkode("noe SB_RV_1")
        assertUgyldigVarselkode("SB_RV_1_EN")
        assertUgyldigVarselkode("SB_RV_1 noe")
        assertUgyldigVarselkode("SBW_RV_1")
        assertUgyldigVarselkode("SB_RVW_1")
        assertUgyldigVarselkode("SB1_RV_1")
        assertUgyldigVarselkode("SB_RV1_1")
        assertUgyldigVarselkode("SB_RV_A1")
        assertUgyldigVarselkode("SB_RV_1A")
        assertUgyldigVarselkode("SBRV_1")
        assertUgyldigVarselkode("SB_RV1")
        assertUgyldigVarselkode("sB_RV_1")
        assertUgyldigVarselkode("Sb_RV_1")
        assertUgyldigVarselkode("SB_rV_1")
        assertUgyldigVarselkode("SB_Vv_1")
        assertUgyldigVarselkode("sb_rv_1")
    }

    private fun assertUgyldigVarselkode(kode: String) {
        assertThrows<IllegalArgumentException> {
            Varselkode(kode)
        }
    }
}