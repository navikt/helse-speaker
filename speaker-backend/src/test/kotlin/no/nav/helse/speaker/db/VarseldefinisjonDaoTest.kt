package no.nav.helse.speaker.db

import no.nav.helse.speaker.domene.Kontekst
import no.nav.helse.speaker.domene.Subdomene
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime.now
import java.util.*

internal class VarseldefinisjonDaoTest: AbstractDatabaseTest() {
    private val definisjonDao = VarseldefinisjonDao(dataSource)

    @Test
    fun `oppretter og finner alle varselkoder`() {
        definisjonDao.opprett(varselkode("RV_IM_1"))
        definisjonDao.opprett(varselkode("SB_EX_1"))
        definisjonDao.opprett(varselkode("SB_EX_2"))
        definisjonDao.opprett(varselkode("SB_EX_3"))
        val funnet = definisjonDao.finnVarselkoder()
        assertEquals(setOf(varselkode("RV_IM_1"), varselkode("SB_EX_1"), varselkode("SB_EX_2"), varselkode("SB_EX_3")), funnet)
    }

    @Test
    fun `oppdaterer varselkode`() {
        val opprinneligVarselkode = varselkode("RV_IM_1")
        definisjonDao.opprett(opprinneligVarselkode)
        val oppdatertVarselkode = varselkode("RV_IM_1").also {
            it.håndter(Varseldefinisjon(UUID.randomUUID(), "RV_IM_1", "EN NY TITTEL", null, null, false, emptyList(), now()))
        }
        definisjonDao.oppdater(oppdatertVarselkode)
        val funnet = definisjonDao.finnVarselkoder()
        assertEquals(setOf(oppdatertVarselkode), funnet)
        assertNotEquals(opprinneligVarselkode, oppdatertVarselkode)
    }

    @Test
    fun `finner ingen varselkoder dersom det ikke finnes noen`() {
        val funnet = definisjonDao.finnVarselkoder()
        assertEquals(emptySet<Varselkode>(), funnet)
    }

    @Test
    fun `kan opprette subdomener`() {
        definisjonDao.opprettSubdomene("navn", "SB")
        val subdomener = definisjonDao.finnSubdomener()
        assertTrue(subdomener.contains(Subdomene("navn", "SB")))
    }

    @Test
    fun `kan opprette kontekster`() {
        definisjonDao.opprettSubdomene("navn", "SB")
        definisjonDao.opprettKontekst(navn = "navn", forkortelse = "AA", subdomene = "SB")
        val subdomener = definisjonDao.finnSubdomener()
        assertEquals(setOf(Subdomene("navn", "SB", setOf(Kontekst("navn", "AA")))), subdomener)
    }

    private fun varselkode(kode: String): Varselkode {
        return Varselkode(Varseldefinisjon(UUID.randomUUID(), kode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false, emptyList(), now()))
    }
}