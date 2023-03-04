package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable

@Serializable
internal data class Varseldefinisjon(
    private val varselkode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean
) {
    internal fun kode() = varselkode

    internal fun lagre(varselRepository: VarselRepository) {
        varselRepository.nytt(varselkode, tittel, forklaring, handling)
    }

    internal fun finnGjeldendeDefinisjon(varselRepository: VarselRepository): Varseldefinisjon {
        return varselRepository.finnGjeldendeDefinisjonFor(varselkode)
    }

    internal fun oppdaterDefinisjon(
        nyDefinisjon: Varseldefinisjon,
        oppdaterBlock: (varselkode: String, tittel: String, forklaring: String?, handling: String?) -> Unit
    ): Boolean {
        if (this.tittel == nyDefinisjon.tittel && this.forklaring == nyDefinisjon.forklaring && this.handling == nyDefinisjon.handling) return false
        oppdaterBlock(varselkode, nyDefinisjon.tittel, nyDefinisjon.forklaring, nyDefinisjon.handling)
        return true
    }

    internal fun oppdaterAvviklet(
        nyDefinisjon: Varseldefinisjon,
        oppdaterBlock: (varselkode: String, avviklet: Boolean) -> Unit
    ): Boolean {
        if (this.avviklet == nyDefinisjon.avviklet) return false
        oppdaterBlock(varselkode, nyDefinisjon.avviklet)
        return true
    }

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
}