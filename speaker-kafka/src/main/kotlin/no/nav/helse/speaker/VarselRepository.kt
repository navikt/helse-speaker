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
        fun List<JsonNode>.varsler(repository: VarselRepository): List<Varsel> {
            //TODO: Slack-integrasjon for ikke-gyldige varselkoder
            val (gyldige, ikkeGyldige) = map { it.fromJson(repository) }.partition(Varsel::erGyldig)
            ikkeGyldige.loggUgyldig()
            return gyldige
        }
    }
}

internal class ActualVarselRepository(dataSource: () -> DataSource): VarselRepository {
    private val varselDao = VarselDao(dataSource())

    override fun opprett(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): Varsel {
        return varselDao.byggVarsel(id, kode, melding, kontekster, tidsstempel)
    }
}