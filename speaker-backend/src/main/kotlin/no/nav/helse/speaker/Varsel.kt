package no.nav.helse.speaker

import kotlinx.serialization.Serializable

@Serializable
internal data class Varsel(
    private val varselkode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean
) {
    internal fun lagre(varselRepository: VarselRepository) {
        varselRepository.nytt(varselkode, tittel, forklaring, handling)
    }

    internal fun oppdater(varselRepository: VarselRepository) {
        varselRepository.oppdater(varselkode, tittel, forklaring, handling, avviklet)
    }
}