package no.nav.helse.speaker

fun main() {
    val envs = mapOf(
        "SANITY_PROJECT_ID" to "z9kr8ddn",
        "SANITY_DATASET" to "production",
        "NAIS_CLUSTER_NAME" to "local",
    )
    val sender = object : Sender("foo", "bar") {
        override fun send(melding: String) {
            throw Exception("En feil har oppstått. Å nei!")
        }
    }

    val bøtte = object : Bøtte {}
    app(envs, sender, bøtte)
}
