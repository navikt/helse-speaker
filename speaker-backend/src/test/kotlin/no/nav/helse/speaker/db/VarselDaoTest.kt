package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.Varsel
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class VarselDaoTest: AbstractDatabaseTest() {
    private val varselDao = VarselDao(dataSource)

    @Test
    fun `kan opprette nytt varsel`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf("EN_FORKLARING"), listOf("EN_HANDLING"), false)
        assertEndretTidspunkt(varselkode, false)
    }

    @Test
    fun `får feil dersom kode finnes fra før`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertThrows<VarselException.KodeEksisterer> {
            varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        }
    }

    @Test
    fun `oppdater avviklet`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", true)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf("EN_FORKLARING"), listOf("EN_HANDLING"), true)
        assertEndretTidspunkt(varselkode, true)
    }

    @Test
    fun `oppdater tittel`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_ANNEN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertVarsel(varselkode, listOf("EN_TITTEL", "EN_ANNEN_TITTEL"), listOf("EN_FORKLARING"), listOf("EN_HANDLING"), false)
    }

    @Test
    fun `oppdater forklaring`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf("EN_FORKLARING", "EN_ANNEN_FORKLARING"), listOf("EN_HANDLING"), false)
    }

    @Test
    fun `oppdater handling`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_ANNEN_HANDLING", false)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf("EN_FORKLARING"), listOf("EN_HANDLING", "EN_ANNEN_HANDLING"), false)
    }

    @Test
    fun `fjern forklaring`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", null, "EN_HANDLING", false)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf("EN_FORKLARING", null), listOf("EN_HANDLING"), false)
    }

    @Test
    fun `oppdater tittel - deretter oppdater tilbake til gammel verdi`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", null, null, false)
        varselDao.oppdaterVarsel(varselkode, "EN_ANNEN_TITTEL", null, null, false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", null, null, false)
        assertEquals(listOf(Varsel(varselkode, "EN_TITTEL", null, null, false)), varselDao.finnVarsler())
    }

    @Test
    fun `ingen forklaring - deretter forklaring - deretter fjernes denne igjen`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", null, "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", null, "EN_HANDLING", false)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf(null, "EN_FORKLARING", null), listOf("EN_HANDLING"), false)
    }

    @Test
    fun `ingen handling - deretter handling - deretter fjernes denne igjen`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", null, false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        varselDao.oppdaterVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", null, false)
        assertVarsel(varselkode, listOf("EN_TITTEL"), listOf("EN_FORKLARING"), listOf(null, "EN_HANDLING", null), false)
    }

    @Test
    fun `finner varsler`() {
        varselDao.nyttVarsel("EN_KODE", "EN_TITTEL", "EN_FORKLARING", null, false)
        varselDao.nyttVarsel("EN_ANNEN_KODE", "EN_ANNEN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false)
        val varsler = varselDao.finnVarsler()
        assertEquals(2, varsler.size)
        assertEquals(listOf(
            Varsel("EN_KODE", "EN_TITTEL", "EN_FORKLARING", null, false),
            Varsel("EN_ANNEN_KODE", "EN_ANNEN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false),
        ), varsler)
    }

    private fun assertEndretTidspunkt(kode: String, erSatt: Boolean) {
        @Language("PostgreSQL")
        val query = "SELECT endret FROM varselkode WHERE kode = ?"
        val endretFinnes = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.localDateTimeOrNull(1) }.asSingle)
        }
        assertEquals(erSatt, endretFinnes != null)
    }

    private fun assertVarsel(kode: String, tittel: List<String>, forklaring: List<String?>, handling: List<String?>, avviklet: Boolean) {
        assertTrue(finnVarselkode(kode))
        assertEquals(avviklet, finnAvviklet(kode))
        assertEquals(tittel, finnVarseltittel(kode))
        assertEquals(forklaring, finnVarselforklaring(kode))
        assertEquals(handling, finnVarselhandling(kode))
    }

    private fun finnVarselkode(kode: String): Boolean {
        @Language("PostgreSQL")
        val query = "SELECT 1 FROM varselkode WHERE kode = ?"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.boolean(1) }.asSingle) ?: false
        }
    }

    private fun finnAvviklet(kode: String): Boolean {
        @Language("PostgreSQL")
        val query = "SELECT avviklet FROM varselkode WHERE kode = ?"
        return sessionOf(dataSource).use { session ->
            requireNotNull(session.run(queryOf(query, kode).map { it.boolean(1) }.asSingle)) { "Fant ikke avviklet for varselkode $kode" }
        }
    }

    private fun finnVarseltittel(kode: String): List<String> {
        @Language("PostgreSQL")
        val query = "SELECT tittel FROM varsel_tittel vt INNER JOIN varselkode v on v.id = vt.varselkode_ref WHERE v.kode = ?"
        return sessionOf(dataSource).use { session ->
            requireNotNull(session.run(queryOf(query, kode).map { it.string(1) }.asList)) { "Fant ikke tittel for varselkode $kode" }
        }
    }

    private fun finnVarselforklaring(kode: String): List<String?> {
        @Language("PostgreSQL")
        val query = "SELECT vk.id, forklaring FROM varselkode vk LEFT JOIN varsel_forklaring vf on vk.id = vf.varselkode_ref WHERE vk.kode = ?"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.long(1) to it.stringOrNull(2) }.asList).map { it.second }
        }
    }

    private fun finnVarselhandling(kode: String): List<String?> {
        @Language("PostgreSQL")
        val query = "SELECT vk.id, handling FROM varselkode vk LEFT JOIN varsel_handling vh on vk.id = vh.varselkode_ref WHERE vk.kode = ?"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.long(1) to it.stringOrNull(2) }.asList).map { it.second }
        }
    }
}