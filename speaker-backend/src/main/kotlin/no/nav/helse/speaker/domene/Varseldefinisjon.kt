@file:UseSerializers(UUIDSerializer::class, LocalDateTimeSerializer::class)
package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.helse.speaker.felles.LocalDateTimeSerializer
import no.nav.helse.speaker.felles.UUIDSerializer
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*

@Serializable
internal class Varseldefinisjon(
    private val id: UUID,
    private val varselkode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean,
    private val forfattere: List<Bruker>,
    private val opprettet: LocalDateTime
) {

    internal fun kode() = varselkode
    internal fun erAvviklet() = avviklet

    override fun equals(other: Any?) =
        this === other || (
            other is Varseldefinisjon
            && javaClass == other.javaClass
            && varselkode == other.varselkode
            && tittel == other.tittel
            && forklaring == other.forklaring
            && handling == other.handling
            && avviklet == other.avviklet
        )

    override fun hashCode(): Int {
        var result = varselkode.hashCode()
        result = 31 * result + tittel.hashCode()
        result = 31 * result + (forklaring?.hashCode() ?: 0)
        result = 31 * result + (handling?.hashCode() ?: 0)
        result = 31 * result + avviklet.hashCode()
        return result
    }

    @Serializable
    internal class VarseldefinisjonPayload(
        private val varselkode: String,
        private val tittel: String,
        private val forklaring: String?,
        private val handling: String?,
        private val avviklet: Boolean,
        private val forfattere: List<Bruker>,
    ) {
        internal fun toVarseldefinisjon() =
            Varseldefinisjon(UUID.randomUUID(), varselkode, tittel, forklaring, handling, avviklet, forfattere, now())
    }

    internal fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "kode" to varselkode,
            "tittel" to tittel,
            "forklaring" to forklaring,
            "handling" to handling,
            "avviklet" to avviklet,
            "opprettet" to opprettet
        )
    }
}