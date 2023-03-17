package no.nav.helse.speaker.domene

import no.nav.helse.speaker.domene.Kontekst.Companion.finnesAllerede
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KontekstTest {
    @Test
    fun `Ugyldig navn - tom`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("", "SØ")
        }
    }
    @Test
    fun `Ugyldig navn - kun whitespace`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst(" ", "SØ")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tom`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("Søknad", "")
        }
    }
    @Test
    fun `Ugyldig forkortelse - små bokstaver`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("Søknad", "ab")
        }
    }
    @Test
    fun `Ugyldig forkortelse - én bokstav`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("Søknad", "A")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tre bokstaver`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("Søknad", "AAA")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tre små bokstaver`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("Søknad", "aaa")
        }
    }
    @Test
    fun `Ugyldig forkortelse - tall`() {
        assertThrows<VarselException.UgyldigKontekst> {
            Kontekst("Søknad", "12")
        }
    }
    @Test
    fun `Gyldig forkortelse`() {
        assertDoesNotThrow {
            Kontekst("Søknad", "SB")
        }
    }
    @Test
    fun `registrere og signalisere observer`() {
        var kalt = false
        val observer = object : IVarselkodeObserver {
            override fun nyKontekst(navn: String, forkortelse: String, subdomene: String) {
                kalt = true
            }
        }
        val kontekst = Kontekst("Søknad", "SØ")
        kontekst.register(observer)
        kontekst.opprett("SB")
        assertTrue(kalt)
    }
    @Test
    fun `navn finnes allerede`() {
        val kontekster = setOf(Kontekst("Søknad", "SØ"), Kontekst("Sykmelding", "SY"))
        val kontekst = Kontekst("Søknad", "SA")
        assertTrue(kontekster.finnesAllerede(kontekst))
    }
    @Test
    fun `forkortelse finnes allerede`() {
        val kontekster = setOf(Kontekst("Inntektsmelding", "IM"), Kontekst("Sykmelding", "SY"))
        val kontekst = Kontekst("Søknad", "IM")
        assertTrue(kontekster.finnesAllerede(kontekst))
    }
}