package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PubliserDefinisjonTest {
    private val id = UUID.randomUUID()

    @Test
    fun `definisjon sendes ut på kafka på riktig format`() {
        val testRapid = TestRapid()
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(false, testRapid)
        testRapid.inspektør.assertDefinisjon(harForklaring = true, harHandling = true)
    }

    @Test
    fun `definisjon sendes ut på kafka på riktig format uten forklaring`() {
        val testRapid = TestRapid()
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = false, harHandling = true)
            .forsøkPubliserDefinisjon(false, testRapid)
        testRapid.inspektør.assertDefinisjon(harForklaring = false, harHandling = true)
    }

    @Test
    fun `definisjon sendes ut på kafka på riktig format uten handling`() {
        val testRapid = TestRapid()
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = false)
            .forsøkPubliserDefinisjon(false, testRapid)
        testRapid.inspektør.assertDefinisjon(harForklaring = true, harHandling = false)
    }

    @Test
    fun `Publiser definisjon hvis ikke i produksjonsmiljø`() {
        val testRapid = TestRapid()
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(false, testRapid)
        assertEquals(1, testRapid.inspektør.size)
    }

    @Test
    fun `Publiser definisjon hvis i produksjonsmiljø og definisjon er togglet _på_ i produksjon`() {
        val testRapid = TestRapid()
        varseldefinisjon(skruddPåIProduksjon = true, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(true, testRapid)
        assertEquals(1, testRapid.inspektør.size)
    }

    @Test
    fun `Ikke publiser definisjon hvis i produksjonsmiljø og definisjon er togglet _av_ i produksjon`() {
        val testRapid = TestRapid()
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(true, testRapid)
        assertEquals(0, testRapid.inspektør.size)
    }

    private fun TestRapid.RapidInspector.assertDefinisjon(
        harForklaring: Boolean,
        harHandling: Boolean,
    ) {
        val melding = this.message(0)
        assertEquals("varselkode_ny_definisjon", melding["@event_name"].asText())
        assertEquals("SB_EX_1", melding["varselkode"].asText())
        assertEquals("SB_EX_1", melding["gjeldende_definisjon"]["kode"].asText())
        assertEquals("En tittel", melding["gjeldende_definisjon"]["tittel"].asText())
        if (harForklaring) {
            assertEquals("En forklaring", melding["gjeldende_definisjon"]["forklaring"].asText())
        } else {
            assertNull(melding["gjeldende_definisjon"]["forklaring"])
        }

        if (harHandling) {
            assertEquals("En handling", melding["gjeldende_definisjon"]["handling"].asText())
        } else {
            assertNull(melding["gjeldende_definisjon"]["handling"])
        }

        assertEquals(false, melding["gjeldende_definisjon"]["avviklet"].asBoolean())
    }

    private fun varseldefinisjon(
        skruddPåIProduksjon: Boolean,
        harForklaring: Boolean,
        harHandling: Boolean,
    ): Varseldefinisjon {
        return Varseldefinisjon(
            _id = id,
            _rev = "en_versjon",
            tittel = "En tittel",
            avviklet = false,
            forklaring = if (harForklaring) "En forklaring" else null,
            handling = if (harHandling) "En handling" else null,
            iProduksjon = skruddPåIProduksjon,
            _updatedAt = LocalDateTime.now(),
            varselkode =
                Varselkode(
                    "SB_EX_1",
                ),
        )
    }
}
