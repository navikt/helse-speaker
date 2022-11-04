package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.speaker.UgyldigVarsel.Companion.fromJson
import no.nav.helse.speaker.UgyldigVarsel.Companion.loggUgyldige
import no.nav.helse.speaker.db.VarselDao
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal interface VarselRepository {
    fun sjekkGyldighet(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): UgyldigVarsel?

    fun definisjoner(): List<Varseldefinisjon>

    companion object {
        fun List<JsonNode>.validerVarsler(repository: VarselRepository) {
            //TODO: Slack-integrasjon for ikke-gyldige varselkoder
            mapNotNull { it.fromJson(repository) }.loggUgyldige()
        }
    }
}

internal class ActualVarselRepository(dataSource: () -> DataSource): VarselRepository {
    private val varselDao = VarselDao(dataSource())

    override fun sjekkGyldighet(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime): UgyldigVarsel? {
        return varselDao.sjekkGyldighet(id, kode, melding, tidsstempel)
    }

    override fun definisjoner(): List<Varseldefinisjon> {
        return varselDao.finnDefinisjoner()
    }
}