package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.LocalDateTimeSerializer
import no.nav.helse.speaker.UUIDSerializer
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

@Serializable
internal class Varseldefinisjon private constructor(
    @Serializable(with = UUIDSerializer::class)
    private val id: UUID = UUID.randomUUID(),
    private val varselkode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean,
    private val forfattere: List<Bruker>,
    @Serializable(with = LocalDateTimeSerializer::class)
    private val opprettet: LocalDateTime = LocalDateTime.now()
) {

    internal constructor(
        id: UUID,
        varselkode: String,
        tittel: String,
        forklaring: String?,
        handling: String?,
        avviklet: Boolean,
        opprettet: LocalDateTime = LocalDateTime.now()
    ): this(id, varselkode, tittel, forklaring, handling, avviklet, emptyList(), opprettet)

    internal fun kode() = varselkode

    internal fun erAvviklet() = avviklet

    override fun equals(other: Any?) =
        this === other || (
            other is Varseldefinisjon
            && javaClass == other.javaClass
            && varselkode == other.varselkode
            && tittel == other.tittel
            && forklaring == other.forklaring
            && handling == other.handling
            && avviklet == other.avviklet
        )

    override fun hashCode(): Int {
        var result = varselkode.hashCode()
        result = 31 * result + tittel.hashCode()
        result = 31 * result + (forklaring?.hashCode() ?: 0)
        result = 31 * result + (handling?.hashCode() ?: 0)
        result = 31 * result + avviklet.hashCode()
        return result
    }

    internal companion object {
        internal fun List<Varseldefinisjon>.konverterTilVarselkode(dataSource: DataSource): List<Varselkode> {
            return groupBy { it.varselkode }.map {(varselkode, definisjoner) ->
                val opprettet = finnOpprettetFor(varselkode, dataSource)
                Varselkode(varselkode, definisjoner, opprettet)
            }
        }

        private fun finnOpprettetFor(varselkode: String, dataSource: DataSource): LocalDateTime {
            @Language("PostgreSQL")
            val query = "SELECT opprettet FROM varselkode WHERE kode = :varselkode"
            return requireNotNull(sessionOf(dataSource).use { session ->
                session.run(queryOf(query, mapOf("varselkode" to varselkode)).map { it.localDateTime("opprettet") }.asSingle)
            })
        }
    }
}