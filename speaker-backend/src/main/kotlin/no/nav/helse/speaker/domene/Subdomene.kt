package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable

@Serializable
internal class Subdomene(
    private val navn: String,
    private val forkortelse: String,
    private val kontekster: Set<Kontekst>
) {
}

@Serializable
internal class Kontekst(
    private val navn: String,
    private val forkortelse: String,
)