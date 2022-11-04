package no.nav.helse.speaker.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class VarselDaoTest: AbstractDatabaseTest() {

    private val dao = VarselDao(dataSource)

    @Test
    fun `gyldig varselkode`() {
        opprettVarsel("EN_KODE")
        val varsel = dao.sjekkGyldighet(UUID.randomUUID(), "EN_KODE", "EN_MELDING", LocalDateTime.now())
        assertNull(varsel)
    }

    @Test
    fun `ugyldig varselkode medfører ugyldig varsel`() {
        opprettVarsel("EN_KODE")
        val varsel = dao.sjekkGyldighet(UUID.randomUUID(), "EN_ANNEN_KODE", "EN_MELDING", LocalDateTime.now())
        assertNotNull(varsel)
    }

    @Test
    fun `finner definisjoner`() {
        opprettVarsel("EN_KODE")
        opprettVarsel("EN_ANNEN_KODE")

        val definisjoner = dao.finnDefinisjoner()
        assertEquals(2, definisjoner.size)
    }

    @Test
    fun `finner alltid kun siste definisjon av et varsel`() {
        val varselkode = "EN_KODE"
        opprettVarsel(varselkode)
        opprettForklaring(varselkode, "EN_NY_FORKLARING")
        opprettTittel(varselkode, "EN_NY_TITTEL")
        opprettForklaring(varselkode, "ENDA_EN_NY_FORKLARING")

        val definisjoner = dao.finnDefinisjoner()
        assertEquals(1, definisjoner.size)
    }
}