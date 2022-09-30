package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
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
        assertVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
    }

    @Test
    fun `får feil dersom kode finnes fra før`() {
        val varselkode = "EN_KODE"
        varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        assertThrows<VarselException.KodeEksisterer> {
            varselDao.nyttVarsel(varselkode, "EN_TITTEL", "EN_FORKLARING", "EN_HANDLING", false)
        }
    }

    private fun assertVarsel(kode: String, tittel: String, forklaring: String?, handling: String, avviklet: Boolean) {
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

    private fun finnVarseltittel(kode: String): String {
        @Language("PostgreSQL")
        val query = "SELECT tittel FROM varsel_tittel INNER JOIN varselkode v on v.id = varsel_tittel.varselkode_ref WHERE v.kode = ?"
        return sessionOf(dataSource).use { session ->
            requireNotNull(session.run(queryOf(query, kode).map { it.string(1) }.asSingle)) { "Fant ikke tittel for varselkode $kode" }
        }
    }

    private fun finnVarselforklaring(kode: String): String? {
        @Language("PostgreSQL")
        val query = "SELECT forklaring FROM varsel_forklaring INNER JOIN varselkode v on v.id = varsel_forklaring.varselkode_ref WHERE v.kode = ?"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.string(1) }.asSingle)
        }
    }

    private fun finnVarselhandling(kode: String): String? {
        @Language("PostgreSQL")
        val query = "SELECT handling FROM varsel_handling INNER JOIN varselkode v on v.id = varsel_handling.varselkode_ref WHERE v.kode = ?"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, kode).map { it.string(1) }.asSingle)
        }
    }
}