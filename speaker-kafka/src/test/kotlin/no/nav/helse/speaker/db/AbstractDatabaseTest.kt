package no.nav.helse.speaker.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

internal abstract class AbstractDatabaseTest {

    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:14").apply {
            withReuse(true)
            withLabel("app-navn", "sparsom")
            start()
            println("Database: jdbc:postgresql://localhost:$firstMappedPort/test startet opp, credentials: test og test")
        }

        val dataSource =
            HikariDataSource(HikariConfig().apply {
                jdbcUrl = postgres.jdbcUrl
                username = postgres.username
                password = postgres.password
                maximumPoolSize = 5
                minimumIdle = 1
                idleTimeout = 500001
                connectionTimeout = 10000
                maxLifetime = 600001
                initializationFailTimeout = 5000
            })

        init {
            Flyway.configure()
                .dataSource(dataSource)
                .failOnMissingLocations(true)
                .cleanDisabled(false)
                .load()
                .also { it.clean() }
                .migrate()

            createTruncateFunction(dataSource)
        }
    }

    @BeforeEach
    fun resetDatabase() {
        sessionOf(dataSource).use  {
            it.run(queryOf("SELECT truncate_tables()").asExecute)
        }
    }

    protected fun opprettVarsel(kode: String) {
        opprettKode(kode)
        opprettDefinisjon(kode, "EN TITTEL", "EN FORKLARING", "EN HANDLING")
    }

    private fun opprettKode(kode: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varselkode(kode, avviklet, opprettet, endret) VALUES (?, false, now(), null)"
            it.run(queryOf(query, kode).asExecute)
        }
    }

    protected fun opprettDefinisjon(kode: String, tittel: String, forklaring: String, handling: String) {
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val query = "INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?, ?, ?)"
            it.run(queryOf(query, kode, tittel, forklaring, handling).asExecute)
        }
    }
}

private fun createTruncateFunction(dataSource: DataSource) {
    sessionOf(dataSource).use  {
        @Language("PostgreSQL")
        val query = """
            CREATE OR REPLACE FUNCTION truncate_tables() RETURNS void AS $$
            DECLARE
            truncate_statement text;
            BEGIN
                SELECT 'TRUNCATE ' || string_agg(format('%I.%I', schemaname, tablename), ',') || ' RESTART IDENTITY CASCADE' 
                    INTO truncate_statement
                FROM pg_tables
                WHERE schemaname='public';

                EXECUTE truncate_statement;
            END;
            $$ LANGUAGE plpgsql;
        """
        it.run(queryOf(query).asExecute)
    }
}