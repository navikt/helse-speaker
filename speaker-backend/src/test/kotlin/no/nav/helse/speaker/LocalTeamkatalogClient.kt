package no.nav.helse.speaker

import no.nav.helse.speaker.domene.Bruker

internal class LocalTeamkatalogClient: ITeamkatalogClient {
    override fun finnTeammedlemmer(): List<Bruker> {
        return listOf(
            Bruker("sara.saksbehandler@nav.no", "Saksbehandler, Sara", "X123456"),
            Bruker("søren.saksbehandler@nav.no", "Saksbehandler, Søren", "Y123456"),
            Bruker("sandy.saksbehandler@nav.no", "Saksbehandler, Sandy", "XY23456"),
        )
    }
}