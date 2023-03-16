package no.nav.helse.speaker.domene

import no.nav.helse.speaker.domene.Subdomene.Companion.finnesAllerede
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SubdomeneTest {
    @Test
    fun `Ugyldig navn - tom`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("", "SB")
        }
    }
    @Test
    fun `Ugyldig navn - kun whitespace`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene(" ", "SB")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tom`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("Saksbehandling", "")
        }
    }
    @Test
    fun `Ugyldig forkortelse - små bokstaver`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("Saksbehandling", "ab")
        }
    }
    @Test
    fun `Ugyldig forkortelse - én bokstav`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("Saksbehandling", "A")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tre bokstaver`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("Saksbehandling", "AAA")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tre små bokstaver`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("Saksbehandling", "aaa")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tall`() {
        assertThrows<VarselException.UgyldigSubdomene> {
            Subdomene("Saksbehandling", "12")
        }
    }
    @Test
    fun `Gyldig forkortelse`() {
        assertDoesNotThrow {
            Subdomene("Saksbehandling", "SB")
        }
    }
    @Test
    fun `registrere og signalisere observer`() {
        var kalt = false
        val observer = object : IVarselkodeObserver {
            override fun nyttSubdomene(navn: String, forkortelse: String) {
                kalt = true
            }
        }
        val subdomene = Subdomene("Saksbehandling", "SB")
        subdomene.register(observer)
        subdomene.opprett()
        assertTrue(kalt)
    }
    @Test
    fun `navn finnes allerede`() {
        val subdomener = setOf(Subdomene("Saksbehandling", "SB"), Subdomene("Regelverk", "RV"))
        val subdomene = Subdomene("Saksbehandling", "SA")
        assertTrue(subdomener.finnesAllerede(subdomene))
    }
    @Test
    fun `forkortelse finnes allerede`() {
        val subdomener = setOf(Subdomene("Saksbehandling", "SB"), Subdomene("Regelverk", "RV"))
        val subdomene = Subdomene("Et annet navn", "SB")
        assertTrue(subdomener.finnesAllerede(subdomene))
    }
}