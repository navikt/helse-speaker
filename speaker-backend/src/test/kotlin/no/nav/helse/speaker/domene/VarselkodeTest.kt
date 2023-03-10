package no.nav.helse.speaker.domene

import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import no.nav.helse.speaker.domene.Varselkode.Companion.finnSubdomenerOgKontekster
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class VarselkodeTest {

    private val varselkoder = setOf(
        varselkode("AA_BB_1"),
        varselkode("MM_NN_1"),
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
    fun `finner alle kontekster og domener`() {
        val varselkoder = setOf(
            varselkode("AA_BB_1"),
            varselkode("MM_NN_1"),
            varselkode("XX_YY_1"),
            varselkode("XX_YY_2"),
            varselkode("XX_FF_1"),
            varselkode("ÆÆ_ØØ_1"),
            varselkode("ØØ_ÆÆ_1"),
        ).finnSubdomenerOgKontekster()

        assertEquals(setOf("BB"), varselkoder["AA"])
        assertEquals(setOf("NN"), varselkoder["MM"])
        assertEquals(setOf("YY", "FF"), varselkoder["XX"])
        assertEquals(setOf("ØØ"), varselkoder["ÆÆ"])
        assertEquals(setOf("ÆÆ"), varselkoder["ØØ"])
    }

    private fun varselkode(kode: String, tittel: String = "EN_TITTEL"): Varselkode {
        return Varselkode(kode, listOf(Varseldefinisjon(UUID.randomUUID(), kode, tittel, "EN_FORKLARING", "EN_HANDLING", false)), LocalDateTime.now())
    }

    @Test
    fun `gyldige varselkoder`() {
        assertDoesNotThrow {
            varselkode("SB_RV_1")
        }
        assertDoesNotThrow {
            varselkode("SB_RV_12")
        }
        assertDoesNotThrow {
            varselkode("SB_RV_123")
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

    @Test
    fun `referential equals`() {
        val varselkode = varselkode("RV_IM_1")
        assertEquals(varselkode, varselkode)
    }

    @Test
    fun `structural equals`() {
        val varselkode1 = varselkode("RV_IM_1")
        val varselkode2 = varselkode("RV_IM_1")
        assertEquals(varselkode1, varselkode2)
        assertEquals(varselkode1.hashCode(), varselkode2.hashCode())
    }

    @Test
    fun `not equals - kode`() {
        val varselkode1 = varselkode("RV_IM_1")
        val varselkode2 = varselkode("RV_IM_2")
        assertNotEquals(varselkode1, varselkode2)
        assertNotEquals(varselkode1.hashCode(), varselkode2.hashCode())
    }

    @Test
    fun `not equals - definisjoner`() {
        val varselkode1 = varselkode("RV_IM_1", "EN_TITTEL")
        val varselkode2 = varselkode("RV_IM_1", "EN_ANNEN_TITTEL")
        assertNotEquals(varselkode1, varselkode2)
        assertNotEquals(varselkode1.hashCode(), varselkode2.hashCode())
    }

    private fun assertUgyldigVarselkode(kode: String) {
        assertThrows<IllegalArgumentException> {
            varselkode(kode)
        }
    }
}