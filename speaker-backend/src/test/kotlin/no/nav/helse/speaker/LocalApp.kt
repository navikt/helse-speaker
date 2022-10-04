package no.nav.helse.speaker

import no.nav.helse.speaker.db.AbstractDatabaseTest

internal class LocalApp: AbstractDatabaseTest(doTruncate = false) {
    private val environmentVariables = mapOf(
        "DATABASE_HOST" to host,
        "DATABASE_PORT" to port,
        "DATABASE_DATABASE" to database,
        "DATABASE_USERNAME" to "test",
        "DATABASE_PASSWORD" to "test"
    )

    internal fun start() {
        createApp(environmentVariables)
    }
}

internal fun main() {
    LocalApp().start()
}