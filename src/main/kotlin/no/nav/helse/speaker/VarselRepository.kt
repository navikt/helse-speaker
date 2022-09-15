package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.speaker.Varsel.Companion.fromJson
import java.time.LocalDateTime
import java.util.*

internal class VarselRepository {
    internal fun opprett(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime) =
        Varsel.gyldig(id, melding, kode, kontekster, tidsstempel, null, null, deprecated = false)

    companion object {
        fun List<JsonNode>.varsler(repository: VarselRepository): List<Varsel> {
            //TODO: Slack-integrasjon for ikke-gyldige varselkoder
            val (gyldige, ikkeGyldige) = map { it.fromJson(repository) }.partition(Varsel::erGyldig)
            return gyldige
        }
    }
}