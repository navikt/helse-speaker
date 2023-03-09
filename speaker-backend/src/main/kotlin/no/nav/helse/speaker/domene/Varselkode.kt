package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import no.nav.helse.speaker.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
internal class Varselkode(
    private val kode: String,
    private val definisjoner: List<Varseldefinisjon>,
    @Serializable(with = LocalDateTimeSerializer::class)
    private val opprettet: LocalDateTime
) {
    init {
        require(kode.matches(mønster)) { "Ugyldig varselkode-format" }
    }
    private val oppdelt = kode.split("_")
    private val domene = oppdelt[0]
    private val kontekst = oppdelt[1]
    private val nummer = oppdelt[2].toInt()
    private val avviklet = definisjoner.lastOrNull()?.erAvviklet() ?: false

    internal fun kode() = kode

    internal companion object {
        private val mønster = "^[A-ZÆØÅ]{2}_[A-ZÆØÅ]{2}_\\d{1,3}\$".toRegex()
        internal fun Set<Varselkode>.finnNeste(prefix: String): String {
            val oppdelt = prefix.split("_")
            val domene = oppdelt[0]
            val kontekst = oppdelt[1]
            val nesteNummer =
                filter { it.domene == domene && it.kontekst == kontekst }
                    .maxByOrNull { it.nummer }
                    ?.nummer?.plus(1)
                    ?: 1

            return prefix + "_" + nesteNummer
        }

        internal fun Set<Varselkode>.finnSubdomenerOgKontekster(): Map<String, Set<String>> {
            return groupBy { it.domene }
                .mapValues { (_, varselkoder) ->
                    varselkoder.map { it.kontekst }.toSet()
                }
        }
    }

    override fun toString(): String {
        return "$kode, antall definisjoner=${definisjoner.size}"
    }

    override fun equals(other: Any?) = this === other || (
        other is Varselkode
            && javaClass == other.javaClass
            && oppdelt == other.oppdelt
            && domene == other.domene
            && kontekst == other.kontekst
            && nummer == other.nummer
        )

    override fun hashCode(): Int {
        var result = oppdelt.hashCode()
        result = 31 * result + domene.hashCode()
        result = 31 * result + kontekst.hashCode()
        result = 31 * result + nummer
        return result
    }


}