package no.nav.helse.speaker.domene

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BrukerTest {

    @Test
    fun `referential equals`() {
        val bruker = Bruker("epost", "navn", "ident")
        assertEquals(bruker, bruker)
    }

    @Test
    fun `structural equals`() {
        val enBruker = Bruker("epost", "navn", "ident")
        val annenBruker = Bruker("epost", "navn", "ident")
        assertEquals(enBruker, annenBruker)
        assertEquals(enBruker.hashCode(), annenBruker.hashCode())
    }

    @Test
    fun `not equals epost`() {
        val enBruker = Bruker("epost", "navn", "ident")
        val annenBruker = Bruker("annen epost", "navn", "ident")
        assertNotEquals(enBruker, annenBruker)
        assertNotEquals(enBruker.hashCode(), annenBruker.hashCode())
    }

    @Test
    fun `not equals navn`() {
        val enBruker = Bruker("epost", "navn", "ident")
        val annenBruker = Bruker("epost", "annet navn", "ident")
        assertNotEquals(enBruker, annenBruker)
        assertNotEquals(enBruker.hashCode(), annenBruker.hashCode())
    }

    @Test
    fun `not equals ident`() {
        val enBruker = Bruker("epost", "navn", "ident")
        val annenBruker = Bruker("epost", "navn", "annen ident")
        assertNotEquals(enBruker, annenBruker)
        assertNotEquals(enBruker.hashCode(), annenBruker.hashCode())
    }
}