package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.Varseldefinisjon
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class VarseldefinisjonDaoTest: AbstractDatabaseTest() {
    private val definisjonDao = VarseldefinisjonDao(dataSource)

    @Test
    fun `kan opprette ny definisjon`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        )
        assertEndretTidspunkt(varselkode, false)
    }

    @Test
    fun `får feil dersom kode finnes fra før`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertThrows<VarselException.KodeEksisterer> {
            definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        }
    }

    @Test
    fun `oppdater avviklet`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterAvviklet(varselkode, true)
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", true),
        )
        assertEndretTidspunkt(varselkode, true)
    }

    @Test
    fun `oppdater tittel`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_ANNEN_TITTEL", "EN_FORKLARING", "EN_HANDLING")
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
            Varseldefinisjon(varselkode, "EN_ANNEN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
        )
    }

    @Test
    fun `oppdater forklaring`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING")
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false),
        )
    }

    @Test
    fun `oppdater handling`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_ANNEN_HANDLING")
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_ANNEN_HANDLING", false),
        )
    }

    @Test
    fun `fjern forklaring`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", null, "EN_HANDLING")
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
            Varseldefinisjon(varselkode, "EN_TITTEL", null, "EN_HANDLING", false),
        )
    }

    @Test
    fun `oppdater tittel - deretter oppdater tilbake til gammel verdi`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", null, null, false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_ANNEN_TITTEL", null, null)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", null, null)
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", null, null, false),
            Varseldefinisjon(varselkode, "EN_ANNEN_TITTEL", null, null, false),
            Varseldefinisjon(varselkode, "EN_TITTEL", null, null, false),
        )
    }

    @Test
    fun `ingen forklaring - deretter forklaring - deretter fjernes denne igjen`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", null, "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING")
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", null, "EN_HANDLING")
        assertVarsel(
            varselkode,
            Varseldefinisjon(varselkode, "EN_TITTEL", null, "EN_HANDLING", false),
            Varseldefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
            Varseldefinisjon(varselkode, "EN_TITTEL", null, "EN_HANDLING", false),
        )
    }

    @Test
    fun `ingen handling - deretter handling - deretter fjernes denne igjen`() {
        val varselkode = "EN_KODE"
        definisjonDao.nyDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", null, false)
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING")
        definisjonDao.oppdaterDefinisjon(varselkode, "EN_TITTEL", "EN_FORKLARING", null)
        assertVarsel(
            varselkode,
            Varseldefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", null, false),
            Varseldefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false),
            Varseldefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", null, false),
        )
    }

    @Test
    fun `finner definisjoner`() {
        definisjonDao.nyDefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", null, false)
        definisjonDao.nyDefinisjon("EN_ANNEN_KODE", "EN_ANNEN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false)
        val definisjoner = definisjonDao.finnDefinisjoner()
        assertEquals(2, definisjoner.size)
        assertEquals(listOf(
            Varseldefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", null, false),
            Varseldefinisjon("EN_ANNEN_KODE", "EN_ANNEN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING", false),
        ), definisjoner)
    }

    @Test
    fun `finner kun siste versjon av en definisjon når alle definisjoner hentes`() {
        definisjonDao.nyDefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon("EN_KODE", "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING")
        definisjonDao.oppdaterDefinisjon("EN_KODE", "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_ANNEN_HANDLING")
        val definisjoner = definisjonDao.finnDefinisjoner()
        assertEquals(
            listOf(
                Varseldefinisjon("EN_KODE", "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_ANNEN_HANDLING", false)
            ),
            definisjoner
        )
    }

    @Test
    fun `finner kun siste versjon av en definisjon`() {
        definisjonDao.nyDefinisjon("EN_KODE", "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        definisjonDao.oppdaterDefinisjon("EN_KODE", "EN_TITTEL", "EN_ANNEN_FORKLARING", "EN_HANDLING")
        assertEquals(
            Varseldefinisjon(
                "EN_KODE",
                "EN_TITTEL",
                "EN_ANNEN_FORKLARING",
                "EN_HANDLING",
                false
            ),
            definisjonDao.finnSisteDefinisjonFor("EN_KODE")
        )
    }

    private fun assertEndretTidspunkt(kode: String, erSatt: Boolean) {
        @Language("PostgreSQL")
        val query = "SELECT endret FROM varselkode WHERE kode = ?"
        val endretFinnes = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.localDateTimeOrNull(1) }.asSingle)
        }
        assertEquals(erSatt, endretFinnes != null)
    }

    private fun assertVarsel(varselkode: String, vararg forventedeDefinisjoner: Varseldefinisjon) {
        assertTrue(finnVarselkode(varselkode))
        val definisjoner = finnDefinisjoner(varselkode)
        assertEquals(forventedeDefinisjoner.toList(), definisjoner)
    }

    private fun finnVarselkode(kode: String): Boolean {
        @Language("PostgreSQL")
        val query = "SELECT 1 FROM varselkode WHERE kode = ?"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.boolean(1) }.asSingle) ?: false
        }
    }

    private fun finnDefinisjoner(kode: String): List<Varseldefinisjon> {
        @Language("PostgreSQL")
        val query = "SELECT kode, tittel, forklaring, handling, avviklet FROM varsel_definisjon vd INNER JOIN varselkode v on v.id = vd.varselkode_ref WHERE v.kode = ?"
        return sessionOf(dataSource).use { session ->
            requireNotNull(session.run(queryOf(query, kode).map {
                Varseldefinisjon(it.string(1), it.string(2), it.stringOrNull(3), it.stringOrNull(4), it.boolean(5))
            }.asList)) { "Fant ikke tittel for varselkode $kode" }
        }
    }

}