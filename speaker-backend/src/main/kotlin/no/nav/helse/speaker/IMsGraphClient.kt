package no.nav.helse.speaker

import no.nav.helse.speaker.domene.Bruker

internal interface IMsGraphClient {
    fun finnTeammedlemmer(): List<Bruker>
}