package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable

@Serializable
internal data class Subdomene(
    private val navn: String,
    private val forkortelse: String,
    private val kontekster: Set<Kontekst>
) {
}

@Serializable
internal data class Kontekst(
    private val navn: String,
    private val forkortelse: String,
)