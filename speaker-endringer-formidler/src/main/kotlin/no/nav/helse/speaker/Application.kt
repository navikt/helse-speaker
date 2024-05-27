package no.nav.helse.speaker

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.LoggerFactory

internal val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        register(
            object : RapidsConnection.StatusListener {
                override fun onReady(rapidsConnection: RapidsConnection) {
                    app(env, rapidsConnection)
                }
            },
        )
    }
}

fun app(
    env: Map<String, String>,
    rapidsConnection: RapidsConnection,
) {
    val sanityProjectId = env["SANITY_PROJECT_ID"] ?: throw IllegalArgumentException("Mangler sanity projectId")
    val sanityDataset = env["SANITY_DATASET"] ?: throw IllegalArgumentException("Mangler sanity dataset")
    val iProduksjonsmiljø = env["NAIS_CLUSTER_NAME"] == "prod-gcp"

    runBlocking {
        sikkerlogg.info("Etablerer lytter mot Sanity")
        sanityVarselendringerListener(iProduksjonsmiljø, sanityProjectId, sanityDataset, rapidsConnection)
    }
}
