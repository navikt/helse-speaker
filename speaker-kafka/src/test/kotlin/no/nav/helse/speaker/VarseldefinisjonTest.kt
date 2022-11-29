package no.nav.helse.speaker

import no.nav.helse.speaker.Varseldefinisjon.Companion.toJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class VarseldefinisjonTest {

    @Test
    fun `forventede felter`() {
        val opprettet = LocalDateTime.now()
        val definisjonId = UUID.randomUUID()
        val definisjoner = listOf(
            Varseldefinisjon(definisjonId, "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", true, opprettet)
        )
        val json = definisjoner.toJson()["definisjoner"] as List<*>
        val varsel = json[0] as Map<*, *>
        assertEquals(definisjonId, varsel["id"] as UUID)
        assertEquals("EN_KODE", varsel["kode"] as String)
        assertEquals("EN_TITTEL", varsel["tittel"] as String)
        assertEquals("EN_FORKLARING", varsel["forklaring"] as String)
        assertEquals("EN_HANDLING", varsel["handling"] as String)
        assertEquals(true, varsel["avviklet"] as Boolean)
        assertEquals(opprettet, varsel["opprettet"] as LocalDateTime)
    }

    @Test
    fun `flere varsler`() {
        val opprettet = LocalDateTime.now()
        val definisjoner = listOf(
            Varseldefinisjon(UUID.randomUUID(), "EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", true, opprettet),
            Varseldefinisjon(UUID.randomUUID(), "EN_ANNEN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", true, opprettet)
        )
        val json = definisjoner.toJson()["definisjoner"] as List<*>
        assertEquals(2, json.size)
    }
}