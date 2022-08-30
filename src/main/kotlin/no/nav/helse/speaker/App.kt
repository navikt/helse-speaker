package no.nav.helse.speaker

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

private fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        AktivitetRiver(this)
    }
}