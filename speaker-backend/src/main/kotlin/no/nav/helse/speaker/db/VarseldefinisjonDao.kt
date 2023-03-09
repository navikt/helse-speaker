package no.nav.helse.speaker.db

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.domene.Bruker
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.intellij.lang.annotations.Language
import java.util.*
import javax.sql.DataSource

internal class VarseldefinisjonDao(private val dataSource: DataSource) {

    internal fun nyDefinisjon(
        kode: String,
        tittel: String,
        forklaring: String?,
        handling: String?,
        avviklet: Boolean
    ): Boolean {
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
            val query =
                "INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?, ?, ?)"
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
                SELECT kode, tittel, forklaring, handling, avviklet, unik_id, vd.opprettet
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
                        it.boolean("avviklet"),
                        it.localDateTime("opprettet"),
                        session.finnForfattereFor(it.uuid("unik_id"))
                    )
                }.asSingle
            )
        }
    }

    private fun Session.finnForfattereFor(definisjonRef: UUID): List<Bruker> {
        @Language("PostgreSQL")
        val query = """
           SELECT oid, navn, epostadresse, ident 
           FROM bruker b 
           INNER JOIN forfatter f on b.oid = f.bruker_ref
           WHERE f.definisjon_ref = :definisjon_ref
        """
        return run(queryOf(query, mapOf(":definisjon_ref" to definisjonRef)).map { row ->
            Bruker(
                row.string("epostadresse"),
                row.string("navn"),
                row.string("ident"),
                row.uuid("oid"),
            )
        }.asList)
    }

    internal fun lagre(kode: String, varselkode: Varselkode) {
        @Language("PostgreSQL")
        val query = "INSERT INTO varseldefinisjon(varselkode, json) VALUES (:kode, :json::json) ON CONFLICT DO NOTHING"
        sessionOf(dataSource).use {
            it.run(queryOf(query, mapOf(
                "kode" to kode,
                "json" to Json.encodeToString(varselkode)
            )).asUpdate)
        }
    }

    internal fun finnAlleDefinisjoner(): List<Varseldefinisjon> {
        return sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                @Language("PostgreSQL")
                val query = """
                SELECT vk.kode, tittel, forklaring, handling, vk.avviklet, unik_id, vd.opprettet
                FROM varselkode vk INNER JOIN varsel_definisjon vd ON vk.id = vd.varselkode_ref ORDER BY vd.id"""

                tx.run(queryOf(query).map { row ->
                    Varseldefinisjon(
                        varselkode = row.string("kode"),
                        tittel = row.string("tittel"),
                        forklaring = row.stringOrNull("forklaring"),
                        handling = row.stringOrNull("handling"),
                        avviklet = row.boolean("avviklet"),
                        opprettet = row.localDateTime("opprettet"),
                        forfattere = emptyList(),
                    )
                }.asList)
            }
        }
    }

    internal fun finnDefinisjoner(): List<Varseldefinisjon> {
        return sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                @Language("PostgreSQL")
                val query = """
                SELECT vk.kode, tittel, forklaring, handling, vk.avviklet, unik_id, opprettet
                FROM varselkode vk INNER JOIN LATERAL (
                    SELECT tittel, forklaring, handling, varselkode_ref, unik_id FROM varsel_definisjon vd WHERE vd.varselkode_ref = vk.id ORDER BY vd.id DESC LIMIT 1
                ) rad ON rad.varselkode_ref = vk.id
                """

                tx.run(queryOf(query).map { row ->
                    Varseldefinisjon(
                        varselkode = row.string("kode"),
                        tittel = row.string("tittel"),
                        forklaring = row.stringOrNull("forklaring"),
                        handling = row.stringOrNull("handling"),
                        avviklet = row.boolean("avviklet"),
                        opprettet = row.localDateTime("opprettet"),
                        forfattere = session.finnForfattereFor(row.uuid("unik_id"))
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
        val query =
            "INSERT INTO varsel_definisjon(varselkode_ref, tittel, forklaring, handling) VALUES ((SELECT id FROM varselkode WHERE kode = ?), ?, ?, ?)"
        return run(queryOf(query, varselkode, tittel, forklaring, handling).asUpdateAndReturnGeneratedKey) != null
    }


    internal fun finnVarselkoder(): Set<Varselkode> {
        @Language("PostgreSQL")
        val query = "SELECT kode, opprettet FROM varselkode"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { Varselkode(it.string("kode"), emptyList(), it.localDateTime("opprettet")) }.asList)
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