package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.speaker.LocalDateTimeSerializer
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import javax.sql.DataSource

@Serializable
internal class Varseldefinisjon(
    private val varselkode: String,
    private val tittel: String,
    private val forklaring: String?,
    private val handling: String?,
    private val avviklet: Boolean,
    @Serializable(with = LocalDateTimeSerializer::class)
    private val opprettet: LocalDateTime,
    private val forfattere: List<Bruker>
) {
    internal fun kode() = varselkode

    internal fun erAvviklet() = avviklet

    internal fun lagre(varselRepository: VarselRepository): Boolean {
        return varselRepository.ny(varselkode, tittel, forklaring, handling)
    }

    internal fun finnGjeldendeDefinisjon(varselRepository: VarselRepository): Varseldefinisjon {
        return varselRepository.finnGjeldendeDefinisjonFor(varselkode)
    }

    internal fun oppdaterDefinisjon(
        nyDefinisjon: Varseldefinisjon,
        oppdaterBlock: (varselkode: String, tittel: String, forklaring: String?, handling: String?) -> Unit
    ): Boolean {
        if (this.tittel == nyDefinisjon.tittel && this.forklaring == nyDefinisjon.forklaring && this.handling == nyDefinisjon.handling) return false
        oppdaterBlock(varselkode, nyDefinisjon.tittel, nyDefinisjon.forklaring, nyDefinisjon.handling)
        return true
    }

    internal fun oppdaterAvviklet(
        nyDefinisjon: Varseldefinisjon,
        oppdaterBlock: (varselkode: String, avviklet: Boolean) -> Unit
    ): Boolean {
        if (this.avviklet == nyDefinisjon.avviklet) return false
        oppdaterBlock(varselkode, nyDefinisjon.avviklet)
        return true
    }

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