package no.nav.helse.speaker

import no.nav.helse.speaker.domene.Varseldefinisjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class VarseldefinisjonTest {
    @Test
    fun `referential equals`() {
        val definisjon = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertEquals(definisjon, definisjon)
        assertEquals(definisjon.hashCode(), definisjon.hashCode())
    }
    @Test
    fun `structural equals`() {
        val definisjon1 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        val definisjon2 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertEquals(definisjon1, definisjon2)
        assertEquals(definisjon1.hashCode(), definisjon2.hashCode())
    }
    @Test
    fun `forskjellig kode`() {
        val definisjon1 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        val definisjon2 = Varseldefinisjon(UUID.randomUUID(), "EN_ANNEN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertNotEquals(definisjon1, definisjon2)
        assertNotEquals(definisjon1.hashCode(), definisjon2.hashCode())
    }
    @Test
    fun `forskjellig tittel`() {
        val definisjon1 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        val definisjon2 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_ANNEN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertNotEquals(definisjon1, definisjon2)
        assertNotEquals(definisjon1.hashCode(), definisjon2.hashCode())
    }
    @Test
    fun `forskjellig forklaring`() {
        val definisjon1 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        val definisjon2 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false)
        assertNotEquals(definisjon1, definisjon2)
        assertNotEquals(definisjon1.hashCode(), definisjon2.hashCode())
    }
    @Test
    fun `forskjellig handling`() {
        val definisjon1 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        val definisjon2 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_ANNEN_HANDLING", false)
        assertNotEquals(definisjon1, definisjon2)
        assertNotEquals(definisjon1.hashCode(), definisjon2.hashCode())
    }
    @Test
    fun `forskjellig avviklet`() {
        val definisjon1 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        val definisjon2 = Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", true)
        assertNotEquals(definisjon1, definisjon2)
        assertNotEquals(definisjon1.hashCode(), definisjon2.hashCode())
    }
}