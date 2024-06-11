package no.nav.helse.speaker

import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PubliserDefinisjonTest {
    private val id = UUID.randomUUID()
    private val testsender =
        object : Sender("", "") {
            private val meldinger = mutableListOf<String>()

            override fun send(melding: String) {
                meldinger.add(melding)
            }

            val size get() = meldinger.size

            fun assertDefinisjon(
                harForklaring: Boolean,
                harHandling: Boolean,
            ) {
                val meldingSomString = meldinger.first()
                val meldingSomJson = meldingSomString.let { jsonReader.decodeFromString<JsonObject>(it) }
                val melding = meldingSomString.let { jsonReader.decodeFromString<VarseldefinisjonEvent>(it) }
                assertEquals("varselkode_ny_definisjon", melding.eventName)
                assertEquals("SB_EX_1", melding.varselkode)
                val gjeldendeDefinisjon = melding.gjeldendeDefinisjon
                assertEquals("SB_EX_1", gjeldendeDefinisjon.kode)
                assertEquals("En tittel", gjeldendeDefinisjon.tittel)
                if (harForklaring) {
                    assertEquals("En forklaring", gjeldendeDefinisjon.forklaring)
                } else {
                    assertNull(gjeldendeDefinisjon.forklaring)
                }

                if (harHandling) {
                    assertEquals("En handling", gjeldendeDefinisjon.handling)
                } else {
                    assertNull(gjeldendeDefinisjon.handling)
                }

                assertEquals(false, gjeldendeDefinisjon.avviklet)
                assertNotNull(meldingSomJson["system_participating_services"])
                assertNotNull(meldingSomJson["@id"])
                assertNotNull(meldingSomJson["@opprettet"])
            }
        }

    @Test
    fun `definisjon sendes ut på kafka på riktig format`() {
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(false, testsender)
        testsender.assertDefinisjon(harForklaring = true, harHandling = true)
    }

    @Test
    fun `definisjon sendes ut på kafka på riktig format uten forklaring`() {
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = false, harHandling = true)
            .forsøkPubliserDefinisjon(false, testsender)
        testsender.assertDefinisjon(harForklaring = false, harHandling = true)
    }

    @Test
    fun `definisjon sendes ut på kafka på riktig format uten handling`() {
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = false)
            .forsøkPubliserDefinisjon(false, testsender)
        testsender.assertDefinisjon(harForklaring = true, harHandling = false)
    }

    @Test
    fun `Publiser definisjon hvis ikke i produksjonsmiljø`() {
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(false, testsender)
        assertEquals(1, testsender.size)
    }

    @Test
    fun `Publiser definisjon hvis i produksjonsmiljø og definisjon er togglet _på_ i produksjon`() {
        varseldefinisjon(skruddPåIProduksjon = true, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(true, testsender)
        assertEquals(1, testsender.size)
    }

    @Test
    fun `Ikke publiser definisjon hvis i produksjonsmiljø og definisjon er togglet _av_ i produksjon`() {
        varseldefinisjon(skruddPåIProduksjon = false, harForklaring = true, harHandling = true)
            .forsøkPubliserDefinisjon(true, testsender)
        assertEquals(0, testsender.size)
    }

    private fun varseldefinisjon(
        skruddPåIProduksjon: Boolean,
        harForklaring: Boolean,
        harHandling: Boolean,
    ): Varseldefinisjon =
        Varseldefinisjon(
            _id = id,
            _rev = "en_versjon",
            tittel = "En tittel",
            avviklet = false,
            forklaring = if (harForklaring) "En forklaring" else null,
            handling = if (harHandling) "En handling" else null,
            iProduksjon = skruddPåIProduksjon,
            _updatedAt = LocalDateTime.now(),
            varselkode = "SB_EX_1",
        )
}
