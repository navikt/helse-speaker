package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import no.nav.helse.speaker.LocalDateTimeSerializer
import no.nav.helse.speaker.UUIDSerializer
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*

@Serializable
internal class Varseldefinisjon(
    @Serializable(with = UUIDSerializer::class)
    private val id: UUID,
    private val varselkode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean,
    private val forfattere: List<Bruker>,
    @Serializable(with = LocalDateTimeSerializer::class)
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
}