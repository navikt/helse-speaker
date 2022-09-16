package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.util.*

internal class VarselDaoTest: AbstractDatabaseTest() {

    private val dao = VarselDao(dataSource)

    @Test
    fun `gyldig varselkode`() {
        opprettVarsel("EN_KODE")
        val varsel = dao.byggVarsel(UUID.randomUUID(), "EN_KODE", "EN_MELDING", emptyList(), LocalDateTime.now())
        assertTrue(varsel.erGyldig())
    }

    @Test
    fun `ugyldig varselkode medfører ugyldig varsel`() {
        opprettVarsel("EN_KODE")
        val varsel = dao.byggVarsel(UUID.randomUUID(), "EN_ANNEN_KODE", "EN_MELDING", emptyList(), LocalDateTime.now())
        assertFalse(varsel.erGyldig())
    }

    @Test
    fun `mangler tittel for varsel`() {
        opprettKode("EN_KODE")
        assertThrows<IllegalArgumentException> {
            dao.byggVarsel(UUID.randomUUID(), "EN_KODE", "EN_MELDING", emptyList(), LocalDateTime.now())
        }
    }

    private fun opprettVarsel(kode: String) {
        opprettKode(kode)
        opprettTittel(kode, "EN TITTEL")
        opprettForklaring(kode, "EN FORKLARING")
        opprettHandling(kode, "EN HANDLING")
    }

    private fun opprettKode(kode: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varselkode(kode, avviklet, opprettet, endret) VALUES (?, false, now(), null)"
            it.run(queryOf(query, kode).asExecute)
        }
    }

    private fun opprettTittel(kode: String, tittel: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varsel_tittel(varselkode_ref, tittel) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?)"
            it.run(queryOf(query, kode, tittel).asExecute)
        }
    }

    private fun opprettForklaring(kode: String, forklaring: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varsel_forklaring(varselkode_ref, forklaring) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?)"
            it.run(queryOf(query, kode, forklaring).asExecute)
        }
    }

    private fun opprettHandling(kode: String, handling: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varsel_handling(varselkode_ref, handling) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?)"
            it.run(queryOf(query, kode, handling).asExecute)
        }
    }
}