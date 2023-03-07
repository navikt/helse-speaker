package no.nav.helse.speaker.db

import io.ktor.http.HttpStatusCode
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class VarseldefinisjonDao(private val dataSource: DataSource) {

    internal fun nyDefinisjon(kode: String, tittel: String, forklaring: String?, handling: String?, avviklet: Boolean): Boolean {
        return sessionOf(dataSource, returnGeneratedKey = true).use {
            it.transaction { tx ->
                tx.nyVarselkode(kode, avviklet) ?: throw VarselException.KodeEksisterer(kode)
                tx.nyDefinisjon(kode, tittel, forklaring, handling)
            }
        }
    }

    internal fun oppdaterDefinisjon(
        varselkode: String,
        tittel: String,
        forklaring: String?,
        handling: String?,
    ) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = "INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?, ?, ?)"
            session.run(queryOf(query, varselkode, tittel, forklaring, handling).asUpdate)
        }
    }

    internal fun oppdaterAvviklet(varselkode: String, avviklet: Boolean) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = "UPDATE varselkode SET avviklet = ?, endret = now() WHERE kode = ?"
            session.run(queryOf(query, avviklet, varselkode).asUpdate)
        }
    }

    internal fun finnSisteDefinisjonFor(varselkode: String): Varseldefinisjon? {
        @Language("PostgreSQL")
        val query =
            """
                SELECT kode, tittel, forklaring, handling, avviklet 
                FROM varsel_definisjon vd 
                INNER JOIN varselkode v ON vd.varselkode_ref = v.id 
                WHERE v.kode = ? 
                ORDER BY vd.id DESC LIMIT 1
                """
        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(query, varselkode).map {
                    Varseldefinisjon(
                        it.string("kode"),
                        it.string("tittel"),
                        it.stringOrNull("forklaring"),
                        it.stringOrNull("handling"),
                        it.boolean("avviklet")
                    )
                }.asSingle
            )
        }
    }

    internal fun finnDefinisjoner(): List<Varseldefinisjon> {
        return sessionOf(dataSource).use {
            it.transaction { tx ->
                @Language("PostgreSQL")
                val query = """
                SELECT vk.kode, tittel, forklaring, handling, vk.avviklet
                FROM varselkode vk INNER JOIN LATERAL (
                    SELECT tittel, forklaring, handling, varselkode_ref FROM varsel_definisjon vd WHERE vd.varselkode_ref = vk.id ORDER BY vd.id DESC LIMIT 1
                ) rad ON rad.varselkode_ref = vk.id
                """

                tx.run(queryOf(query).map { row ->
                    Varseldefinisjon(
                        varselkode = row.string("kode"),
                        tittel = row.string("tittel"),
                        forklaring = row.stringOrNull("forklaring"),
                        handling = row.stringOrNull("handling"),
                        avviklet = row.boolean("avviklet")
                    )
                }.asList)
            }
        }
    }

    private fun TransactionalSession.nyVarselkode(kode: String, avviklet: Boolean): Long? {
        @Language("PostgreSQL")
        val query = "INSERT INTO varselkode(kode, avviklet) VALUES (?, ?) ON CONFLICT(kode) DO NOTHING"
        return run(queryOf(query, kode, avviklet).asUpdateAndReturnGeneratedKey)
    }

    private fun TransactionalSession.nyDefinisjon(
        varselkode: String,
        tittel: String,
        forklaring: String?,
        handling: String?
    ): Boolean {
        @Language("PostgreSQL")
        val query = "INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?, ?, ?)"
        return run(queryOf(query, varselkode, tittel, forklaring, handling).asUpdateAndReturnGeneratedKey) != null
    }


    internal fun finnVarselkoder(): Set<Varselkode> {
        @Language("PostgreSQL")
        val query = "SELECT kode FROM varselkode"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { Varselkode(it.string("kode")) }.asList)
        }.toSet()
    }
}

internal sealed class VarselException(message: String) : Exception(message) {
    internal abstract val httpStatusCode: HttpStatusCode

    internal class KodeEksisterer(kode: String) : VarselException("Varselkode $kode eksisterer allerede") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.Conflict
    }

    internal class KodeFinnesIkke(kode: String) : VarselException("Varselkode $kode finnes ikke") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotFound
    }

    internal class IngenEndring(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varseldefinisjon $varseldefinisjon er allerede gjeldende definisjon") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotModified
    }

    internal class DefinisjonFinnesIkke(kode: String) : VarselException("Definisjon for varselkode $kode finnes ikke") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotFound
    }
}