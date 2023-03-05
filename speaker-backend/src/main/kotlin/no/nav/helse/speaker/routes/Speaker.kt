package no.nav.helse.speaker.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.domene.VarselRepository


internal fun Route.speaker(azureAD: AzureAD, varselRepository: VarselRepository) {
    route("/api") {
        varselRoutes(varselRepository)
        brukerRoutes(azureAD)
    }
}




