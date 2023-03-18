package no.nav.helse.speaker

import no.nav.helse.speaker.domene.Bruker

internal interface ITeamkatalogClient {
    fun finnTeammedlemmer(): List<Bruker>
}