package no.nav.helse.speaker

import no.nav.helse.speaker.db.AbstractDatabaseTest
import no.nav.security.mock.oauth2.MockOAuth2Server

internal class LocalApp: AbstractDatabaseTest(doTruncate = false) {
    private val oauthMock = OauthMock
    private val environmentVariables: Map<String, String> = mutableMapOf(
        "DATABASE_HOST" to host,
        "DATABASE_PORT" to port,
        "DATABASE_DATABASE" to database,
        "DATABASE_USERNAME" to "test",
        "DATABASE_PASSWORD" to "test",
        "LOCAL_DEVELOPMENT" to "true"
    ).apply {
        putAll(oauthMock.oauthConfig)
    }.toMap()

    internal fun start() {
        createApp(environmentVariables)
    }
}

internal fun main() {
    LocalApp().start()
}

private object OauthMock {
    private val server = MockOAuth2Server().also {
        it.start(0)
    }
    internal val oauthConfig = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to server.wellKnownUrl("default").toString(),
        "AZURE_APP_CLIENT_ID" to "some_client_id",
        "AZURE_APP_CLIENT_SECRET" to "some_secret",
        "AUTHORIZATION_URL" to server.authorizationEndpointUrl("default").toString(),
    )
}