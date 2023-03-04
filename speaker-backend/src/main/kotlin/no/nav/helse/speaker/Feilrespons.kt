package no.nav.helse.speaker

import kotlinx.serialization.Serializable

@Serializable
data class Feilrespons(
    val errorId: String,
    val description: String?
)