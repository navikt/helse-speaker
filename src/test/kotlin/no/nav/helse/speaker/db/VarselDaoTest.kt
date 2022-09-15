package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VarselDaoTest: AbstractDatabaseTest() {

    private val dao = VarselDao(dataSource)

    @Test
    fun `validering av varselkode`() {
        opprettKode("KODE")
        assertTrue(dao.valider("KODE"))
        assertFalse(dao.valider("EDOK"))
    }

    private fun opprettKode(kode: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varselkode(kode, avviklet, opprettet, endret) VALUES (?, false, now(), null)"
            it.run(queryOf(query, kode).asExecute)
        }
    }
}