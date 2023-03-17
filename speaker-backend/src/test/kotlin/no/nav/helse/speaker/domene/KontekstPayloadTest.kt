package no.nav.helse.speaker.domene

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KontekstPayloadTest {
    @Test
    fun `tilhører subdomene`() {
        val kontekstPayload = KontekstPayload(navn = "navn", forkortelse = "AA", subdomene = "BB")
        assertTrue(kontekstPayload.tilhører("BB"))
        assertFalse(kontekstPayload.tilhører("AB"))
        assertFalse(kontekstPayload.tilhører("AA"))
    }
    @Test
    fun `kan lage kontekst`() {
        val kontekstPayload = KontekstPayload(navn = "navn", forkortelse = "AA", subdomene = "BB")
        val kontekst = kontekstPayload.toKontekst()
        assertEquals(Kontekst("navn", "AA"), kontekst)
    }
}