package no.nav.helse.speaker.db

import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime.now

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
            it.håndter(Varseldefinisjon("RV_IM_1", "EN NY TITTEL", null, null, false, now(), emptyList()))
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

    private fun varselkode(kode: String): Varselkode {
        return Varselkode(kode, listOf(Varseldefinisjon(kode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false, now(), emptyList())), now())
    }
}