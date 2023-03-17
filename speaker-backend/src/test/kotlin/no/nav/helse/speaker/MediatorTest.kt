package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.speaker.domene.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class MediatorTest {
    private val testRapid = TestRapid()

    @Test
    fun `publiserer melding på kafka ved beskjed om oppdatert definisjon`() {
        val kode = "XX_YY_1"
        val mediator = Mediator({ testRapid }, repository)
        mediator.varselkodeEndret(varselkode(kode), kode, definisjon(kode))
        assertEquals(1, testRapid.inspektør.size)
        assertEquals("varselkode_ny_definisjon", testRapid.inspektør.message(0)["@event_name"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["varselkode"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["gjeldende_definisjon"]["kode"].asText())
        assertEquals("En tittel", testRapid.inspektør.message(0)["gjeldende_definisjon"]["tittel"].asText())
        assertTrue(testRapid.inspektør.message(0)["gjeldende_definisjon"]["forklaring"].isNull)
        assertTrue(testRapid.inspektør.message(0)["gjeldende_definisjon"]["handling"].isNull)
        assertEquals(false, testRapid.inspektør.message(0)["gjeldende_definisjon"]["avviklet"].asBoolean())
    }

    @Test
    fun `publiserer melding på kafka ved beskjed om oppdatert definisjon når forklaring og handling er satt`() {
        val kode = "XX_YY_1"
        val mediator = Mediator({ testRapid }, repository)
        mediator.varselkodeEndret(varselkode(kode), kode, definisjon(kode, forklaring = "En forklaring", handling = "En handling"))
        assertEquals(1, testRapid.inspektør.size)
        assertEquals("varselkode_ny_definisjon", testRapid.inspektør.message(0)["@event_name"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["varselkode"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["gjeldende_definisjon"]["kode"].asText())
        assertEquals("En tittel", testRapid.inspektør.message(0)["gjeldende_definisjon"]["tittel"].asText())
        assertEquals("En forklaring", testRapid.inspektør.message(0)["gjeldende_definisjon"]["forklaring"].asText())
        assertEquals("En handling", testRapid.inspektør.message(0)["gjeldende_definisjon"]["handling"].asText())
        assertEquals(false, testRapid.inspektør.message(0)["gjeldende_definisjon"]["avviklet"].asBoolean())
    }

    @Test
    fun `oppretter varsel`() {
        val kode = "XX_YY_1"
        val mediator = Mediator({ testRapid }, repository)
        mediator.håndterNyVarselkode(definisjon(kode, forklaring = "En forklaring", handling = "En handling"))
        assertEquals(1, repository.varselOpprettet.size)
    }

    @Test
    fun `oppdaterer varsel`() {
        val kode = "XX_YY_1"
        val mediator = Mediator({ testRapid }, repository)
        mediator.håndterNyVarselkode(definisjon(kode, forklaring = "En forklaring", handling = "En handling"))
        mediator.håndterOppdatertVarselkode(definisjon(kode, forklaring = "En forklaring", handling = "En annen handling"))
        assertEquals(1, repository.varselOpprettet.size)
        assertEquals(1, repository.varselOppdatert.size)
    }

    @Test
    fun `oppretter subdomene`() {
        val mediator = Mediator({ testRapid }, repository)
        mediator.håndterNyttSubdomene(Subdomene("Et subdomene", "XX"))
        assertEquals(1, repository.subdomeneOpprettet.size)
    }

    @Test
    fun `oppretter ikke subdomene hvis det eksisterer fra før av`() {
        val mediator = Mediator({ testRapid }, repository)
        assertThrows<VarselException.SubdomeneFinnesAllerede> {
            mediator.håndterNyttSubdomene(Subdomene("Et subdomene", "AA"))
        }
        assertEquals(0, repository.subdomeneOpprettet.size)
    }

    @Test
    fun `oppretter kontekst`() {
        val mediator = Mediator({ testRapid }, repository)
        mediator.håndterNyKontekst(KontekstPayload(navn = "Et subdomene", forkortelse = "XX", subdomene = "AA"))
        assertEquals(1, repository.kontekstOpprettet.size)
    }

    @Test
    fun `oppretter ikke kontekst hvis det eksisterer fra før av`() {
        val mediator = Mediator({ testRapid }, repository)
        assertThrows<VarselException.KontekstFinnesAllerede> {
            mediator.håndterNyKontekst(KontekstPayload(navn = "Et subdomene", forkortelse = "XX", subdomene = "BB"))
        }
        assertEquals(0, repository.kontekstOpprettet.size)
    }

    @Test
    fun `oppretter ikke kontekst hvis subdomene ikke finnes`() {
        val mediator = Mediator({ testRapid }, repository)
        assertThrows<VarselException.SubdomeneFinnesIkke> {
            mediator.håndterNyKontekst(KontekstPayload(navn = "Et subdomene", forkortelse = "XX", subdomene = "YY"))
        }
        assertEquals(0, repository.kontekstOpprettet.size)
    }

    private fun varselkode(
        kode: String = "AA_BB_1",
        tittel: String = "En tittel",
    ) = Varselkode(definisjon(kode, tittel))

    private fun definisjon(
        kode: String = "AA_BB_1",
        tittel: String = "En tittel",
        forklaring: String? = null,
        handling: String? = null
    ) = Varseldefinisjon(
        id = UUID.randomUUID(),
        varselkode = kode,
        tittel = tittel,
        forklaring = forklaring,
        handling = handling,
        avviklet = false,
        forfattere = emptyList(),
        opprettet = LocalDateTime.now()
    )

    private val repository = object : VarselRepository {
        internal val varselOpprettet = mutableListOf<Varselkode>()
        internal val varselOppdatert = mutableListOf<Varselkode>()
        internal val subdomeneOpprettet = mutableListOf<String>()
        internal val kontekstOpprettet = mutableListOf<String>()
        override fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon> = TODO("Not yet implemented")
        override fun finnNesteVarselkodeFor(prefix: String): String = TODO("Not yet implemented")
        override fun finnSubdomenerOgKontekster(): Set<Subdomene> = setOf(Subdomene("Et subdomene AA", "AA"), Subdomene("Et subdomene BB", "BB", setOf(Kontekst("En kontekst XX", "XX"))))
        override fun finn(varselkode: String): Varselkode? = varselOpprettet.find { it.kode() == varselkode }
        override fun nyVarselkode(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
            varselOpprettet.add(varselkode)
        }

        override fun varselkodeEndret(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
            varselOppdatert.add(varselkode)
        }

        override fun nyttSubdomene(navn: String, forkortelse: String) {
            subdomeneOpprettet.add(navn)
        }

        override fun nyKontekst(navn: String, forkortelse: String, subdomene: String) {
            kontekstOpprettet.add(navn)
        }
    }

}