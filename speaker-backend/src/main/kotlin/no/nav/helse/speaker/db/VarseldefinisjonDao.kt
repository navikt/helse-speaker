package no.nav.helse.speaker.db

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.domene.Kontekst
import no.nav.helse.speaker.domene.Subdomene
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.intellij.lang.annotations.Language
import java.util.*
import javax.sql.DataSource

internal class VarseldefinisjonDao(private val dataSource: DataSource) {

    internal fun finn(kode: String): Varselkode? {
        @Language("PostgreSQL")
        val query = "SELECT json FROM varseldefinisjon WHERE varselkode = :varselkode"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query, mapOf("varselkode" to kode)).map {
                Json.decodeFromString<Varselkode>(it.string("json"))
            }.asSingle)
        }
    }

    internal fun oppdater(varselkode: Varselkode) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query =
                "UPDATE varseldefinisjon SET json = :json::json WHERE varselkode = :varselkode"
            session.run(queryOf(query, mapOf(
                "json" to Json.encodeToString(varselkode),
                "varselkode" to varselkode.kode()
            )).asUpdate)
        }
    }

    internal fun opprett(varselkode: Varselkode) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query =
                "INSERT INTO varseldefinisjon(varselkode, json) VALUES (:varselkode, :json::json)"
            session.run(queryOf(query, mapOf(
                "json" to Json.encodeToString(varselkode),
                "varselkode" to varselkode.kode()
            )).asUpdate)
        }
    }

    internal fun finnVarselkoder(): Set<Varselkode> {
        @Language("PostgreSQL")
        val query = "SELECT json FROM varseldefinisjon"
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { Json.decodeFromString<Varselkode>(it.string("json")) }.asList)
        }.toSet()
    }

    internal fun finnSubdomener(): Set<Subdomene> {
        @Language("PostgreSQL")
        val query = "SELECT id, navn, forkortelse FROM subdomene sd"
        return sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(queryOf(query).map {
                    Subdomene(
                        it.string("navn"),
                        it.string("forkortelse"),
                        tx.finnKontekster(it.uuid("id"))
                    )
                }.asList).toSet()
            }
        }
    }

    private fun TransactionalSession.finnKontekster(id: UUID): Set<Kontekst> {
        @Language("PostgreSQL")
        val query = "SELECT navn, forkortelse FROM kontekst WHERE subdomene_ref = ?"
        return run(queryOf(query, id).map {
            Kontekst(
                it.string("navn"),
                it.string("forkortelse")
            )
        }.asList).toSet()
    }
}

internal sealed class VarselException(message: String) : Exception(message) {
    internal abstract val httpStatusCode: HttpStatusCode

    internal class IngenEndring(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varseldefinisjon $varseldefinisjon er allerede gjeldende definisjon") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotModified
    }

    internal class FinnesIkke(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varselkode for varseldefinisjon $varseldefinisjon finnes ikke") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotFound
    }

    internal class FinnesAllerede(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varselkode for varseldefinisjon $varseldefinisjon finnes fra før av") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.Conflict
    }

    internal class UgyldigSubdomene(subdomene: String?):
        VarselException("Subdomene=$subdomene er ikke på gyldig format.") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }

    internal class UgyldigKontekst(kontekst: String?):
        VarselException("Kontekst=$kontekst er ikke på gyldig format.") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }
}