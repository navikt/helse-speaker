package no.nav.helse.speaker.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import java.time.Duration

internal class DataSourceBuilder(env: Map<String, String>) {
    private val gcpProjectId: String = requireNotNull(env["GCP_TEAM_PROJECT_ID"]) { "gcp project id må settes" }
    private val databaseRegion: String = requireNotNull(env["DATABASE_REGION"]) { "region må settes" }
    private val databaseInstance: String = requireNotNull(env["DATABASE_INSTANCE"]) { "instans må settes" }
    private val databaseUsername: String = requireNotNull(env["DATABASE_SPEAKER_KAFKA_USERNAME"]) { "brukernavn må settes" }
    private val databasePassword: String = requireNotNull(env["DATABASE_SPEAKER_KAFKA_PASSWORD"]) { "passord må settes" }
    private val databaseName: String = requireNotNull(env["DATABASE_NAME"]) { "databasenavn må settes" }

    private val dbUrl = String.format(
        "jdbc:postgresql:///%s?%s&%s",
        databaseName,
        "cloudSqlInstance=$gcpProjectId:$databaseRegion:$databaseInstance",
        "socketFactory=com.google.cloud.sql.postgres.SocketFactory"
    )

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = databaseUsername
        password = databasePassword
        idleTimeout = Duration.ofMinutes(1).toMillis()
        maxLifetime = idleTimeout * 5
        initializationFailTimeout = Duration.ofMinutes(1).toMillis()
        connectionTimeout = Duration.ofSeconds(30).toMillis()
        maximumPoolSize = 1
        metricRegistry = PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            CollectorRegistry.defaultRegistry,
            Clock.SYSTEM
        )
    }

    internal fun getDataSource(): HikariDataSource {
        return HikariDataSource(hikariConfig)
    }

}