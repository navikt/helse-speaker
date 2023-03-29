package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class VarselDaoTest: AbstractDatabaseTest() {

    private val dao = VarselDao(dataSource)

    @Test
    fun `Kan hente ut varselkoder`() {
        opprettVarseldefinisjon("RV_SY_1")
        opprettVarseldefinisjon("RV_SY_2")
        val varselkoder = dao.finnVarselkoderFor(listOf("RV_SY_1", "RV_SY_2"))
        assertEquals(2, varselkoder.size)
    }

    @Test
    fun `finnVarselkoderFor gir tom liste hvis etterspurte varselkoder er tom`() {
        opprettVarseldefinisjon("RV_SY_1")
        val varselkoder = dao.finnVarselkoderFor(emptyList())
        assertEquals(0, varselkoder.size)
    }

    private fun opprettVarseldefinisjon(varselkode: String) {
        @Language("PostgreSQL")
        val query = "INSERT INTO varseldefinisjon(varselkode, json) VALUES (?, ?::json)"
        sessionOf(dataSource).use {
            it.run(queryOf(query, varselkode, testjson(varselkode)).asExecute)
        }
    }

    @Language("JSON")
    private fun testjson(varselkode: String) = """{
  "kode": "$varselkode",
  "opprettet": "2023-3-29T00:00:00.000000",
  "definisjoner": [
    {
      "id": "${UUID.randomUUID()}",
      "tittel": "En tittel",
      "avviklet": false,
      "handling": null,
      "opprettet": "2023-3-29T00:00:00.000000",
      "forfattere": [],
      "forklaring": null,
      "varselkode": "$varselkode"
    },
    {
      "id": "${UUID.randomUUID()}",
      "tittel": "En annen tittel",
      "avviklet": true,
      "handling": null,
      "opprettet": "2023-3-30T00:00:00.000000",
      "forfattere": [],
      "forklaring": null,
      "varselkode": "$varselkode"
    }
  ]
}
    """
}