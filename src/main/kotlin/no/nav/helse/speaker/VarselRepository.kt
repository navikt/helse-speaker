package no.nav.helse.speaker

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*

internal class VarselRepository {
    internal fun opprett(id: UUID, kode: String, melding: String, kontekster: List<JsonNode>, tidsstempel: LocalDateTime) =
        Varsel(id, melding, kode, kontekster, tidsstempel, null, null, gyldig = true, deprecated = false)
}