package no.nav.helse.speaker.routes

import io.ktor.server.http.content.ignoreFiles
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.nav.helse.speaker.azure.AzureAD
import no.nav.helse.speaker.domene.VarselRepository


internal fun Route.speaker(azureAD: AzureAD, varselRepository: VarselRepository, isLocalDevelopment: Boolean) {
    route("/api") {
        singlePageApplication {
            useResources = !isLocalDevelopment
            react("speaker-frontend/dist")
            ignoreFiles { it.endsWith(".txt") }
        }
        varselRoutes(varselRepository)
        brukerRoutes(azureAD)
    }
}




