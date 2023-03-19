package no.nav.helse.speaker

import no.nav.helse.speaker.domene.Bruker
import java.util.*

internal class LocalMsGraphClient: IMsGraphClient {
    override fun finnTeammedlemmer(): List<Bruker> {
        return listOf(
            Bruker("sara.saksbehandler@nav.no", "Sara Saksbehandler", "X123456", UUID.nameUUIDFromBytes("X123456".toByteArray())),
            Bruker("søren.saksbehandler@nav.no", "Søren Saksbehandler", "Y123456", UUID.nameUUIDFromBytes("Y123456".toByteArray())),
            Bruker("sandy.saksbehandler@nav.no", "Sandy Saksbehandler", "XY23456", UUID.nameUUIDFromBytes("XY23456".toByteArray())),
        )
    }
}