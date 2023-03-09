package no.nav.helse.speaker.domene

import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import no.nav.helse.speaker.domene.Varselkode.Companion.finnSubdomenerOgKontekster
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class VarselkodeTest {

    private val varselkoder = setOf(
        Varselkode("AA_BB_1", emptyList(), LocalDateTime.now()),
        Varselkode("MM_NN_1", emptyList(), LocalDateTime.now()),
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
            Varselkode("AA_BB_1", emptyList(), LocalDateTime.now()),
            Varselkode("MM_NN_1", emptyList(), LocalDateTime.now()),
            Varselkode("XX_YY_1", emptyList(), LocalDateTime.now()),
            Varselkode("XX_YY_2", emptyList(), LocalDateTime.now()),
            Varselkode("XX_FF_1", emptyList(), LocalDateTime.now()),
            Varselkode("ÆÆ_ØØ_1", emptyList(), LocalDateTime.now()),
            Varselkode("ØØ_ÆÆ_1", emptyList(), LocalDateTime.now()),
        ).finnSubdomenerOgKontekster()

        assertEquals(setOf("BB"), varselkoder["AA"])
        assertEquals(setOf("NN"), varselkoder["MM"])
        assertEquals(setOf("YY", "FF"), varselkoder["XX"])
        assertEquals(setOf("ØØ"), varselkoder["ÆÆ"])
        assertEquals(setOf("ÆÆ"), varselkoder["ØØ"])
    }

    @Test
    fun `gyldige varselkoder`() {
        assertDoesNotThrow {
            Varselkode("SB_RV_1", emptyList(), LocalDateTime.now())
        }
        assertDoesNotThrow {
            Varselkode("SB_RV_12", emptyList(), LocalDateTime.now())
        }
        assertDoesNotThrow {
            Varselkode("SB_RV_123", emptyList(), LocalDateTime.now())
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
            Varselkode(kode, emptyList(), LocalDateTime.now())
        }
    }
}