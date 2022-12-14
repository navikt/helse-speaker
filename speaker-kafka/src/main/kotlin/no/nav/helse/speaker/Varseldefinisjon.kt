package no.nav.helse.speaker

import java.time.LocalDateTime
import java.util.UUID

internal class Varseldefinisjon(
    private val unikId: UUID,
    private val kode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean,
    private val opprettet: LocalDateTime
) {
    internal companion object {
        internal fun List<Varseldefinisjon>.toJson(): Map<String, Any> {
            return mapOf(
                "definisjoner" to map {
                    mapOf(
                        "id" to it.unikId,
                        "kode" to it.kode,
                        "tittel" to it.tittel,
                        "forklaring" to it.forklaring,
                        "handling" to it.handling,
                        "avviklet" to it.avviklet,
                        "opprettet" to it.opprettet
                    )
                }
            )
        }
    }
}