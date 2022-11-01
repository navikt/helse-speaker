package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.speaker.Varsel.Companion.fromJson
import no.nav.helse.speaker.Varsel.Companion.loggUgyldig
import no.nav.helse.speaker.db.VarselDao
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal interface VarselRepository {
    fun opprett(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): Varsel

    companion object {
        fun List<JsonNode>.validerVarsler(repository: VarselRepository) {
            //TODO: Slack-integrasjon for ikke-gyldige varselkoder
            val ikkeGyldigeVarsler = map { it.fromJson(repository) }.filterNot(Varsel::erGyldig)
            ikkeGyldigeVarsler.loggUgyldig()
        }
    }
}

internal class ActualVarselRepository(dataSource: () -> DataSource): VarselRepository {
    private val varselDao = VarselDao(dataSource())

    override fun opprett(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): Varsel {
        return varselDao.byggVarsel(id, kode, melding, kontekster, tidsstempel)
    }
}