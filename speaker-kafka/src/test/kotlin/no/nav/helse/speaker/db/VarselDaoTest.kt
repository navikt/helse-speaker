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


}