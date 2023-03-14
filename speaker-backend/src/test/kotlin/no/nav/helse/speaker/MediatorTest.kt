package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class MediatorTest {
    private val testRapid = TestRapid()

    @Test
    fun `publiserer melding på kafka ved beskjed om oppdatert definisjon`() {
        val kode = "XX_YY_1"
        val mediator = Mediator(testRapid, repository)
        mediator.varselkodeOppdatert(varselkode(kode), kode, definisjon(kode))
        assertEquals(1, testRapid.inspektør.size)
        assertEquals("varselkode_ny_definisjon", testRapid.inspektør.message(0)["@event_name"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["varselkode"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["gjeldende_definisjon"]["kode"].asText())
        assertEquals("En tittel", testRapid.inspektør.message(0)["gjeldende_definisjon"]["tittel"].asText())
        assertTrue(testRapid.inspektør.message(0)["gjeldende_definisjon"]["forklaring"].isNull)
        assertTrue(testRapid.inspektør.message(0)["gjeldende_definisjon"]["handling"].isNull)
        assertEquals(false, testRapid.inspektør.message(0)["gjeldende_definisjon"]["avviklet"].asBoolean())
    }

    @Test
    fun `publiserer melding på kafka ved beskjed om oppdatert definisjon når forklaring og handling er satt`() {
        val kode = "XX_YY_1"
        val mediator = Mediator(testRapid, repository)
        mediator.varselkodeOppdatert(varselkode(kode), kode, definisjon(kode, forklaring = "En forklaring", handling = "En handling"))
        assertEquals(1, testRapid.inspektør.size)
        assertEquals("varselkode_ny_definisjon", testRapid.inspektør.message(0)["@event_name"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["varselkode"].asText())
        assertEquals(kode, testRapid.inspektør.message(0)["gjeldende_definisjon"]["kode"].asText())
        assertEquals("En tittel", testRapid.inspektør.message(0)["gjeldende_definisjon"]["tittel"].asText())
        assertEquals("En forklaring", testRapid.inspektør.message(0)["gjeldende_definisjon"]["forklaring"].asText())
        assertEquals("En handling", testRapid.inspektør.message(0)["gjeldende_definisjon"]["handling"].asText())
        assertEquals(false, testRapid.inspektør.message(0)["gjeldende_definisjon"]["avviklet"].asBoolean())
    }

    private fun varselkode(
        kode: String = "AA_BB_1",
        tittel: String = "En tittel",
    ) = Varselkode(definisjon(kode, tittel))

    private fun definisjon(
        kode: String = "AA_BB_1",
        tittel: String = "En tittel",
        forklaring: String? = null,
        handling: String? = null
    ) = Varseldefinisjon(
        id = UUID.randomUUID(),
        varselkode = kode,
        tittel = tittel,
        forklaring = forklaring,
        handling = handling,
        avviklet = false,
        forfattere = emptyList(),
        opprettet = LocalDateTime.now()
    )

    private val repository = object : VarselRepository {
        override fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon> = TODO("Not yet implemented")
        override fun oppdater(varselkode: Varselkode): Unit = TODO("Not yet implemented")
        override fun finnNesteVarselkodeFor(prefix: String): String = TODO("Not yet implemented")
        override fun finnSubdomenerOgKontekster(): Map<String, Set<String>> = TODO("Not yet implemented")
        override fun finn(varselkode: String): Varselkode = TODO("Not yet implemented")
        override fun opprett(varselkode: Varselkode): Unit = TODO("Not yet implemented")
    }

}