package no.nav.helse.speaker.db

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
}