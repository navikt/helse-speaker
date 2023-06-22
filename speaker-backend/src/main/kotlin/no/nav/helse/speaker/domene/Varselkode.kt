package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nav.helse.speaker.domene.IVarselkodeObserver.Companion.varselkodeEndret
import no.nav.helse.speaker.domene.IVarselkodeObserver.Companion.varselkodeOpprettet
import no.nav.helse.speaker.felles.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
internal class Varselkode private constructor(
    private val kode: String,
    private val definisjoner: MutableList<Varseldefinisjon>,
    @Serializable(with = LocalDateTimeSerializer::class)
    private val opprettet: LocalDateTime
) {

    constructor(definisjon: Varseldefinisjon, vararg observere: IVarselkodeObserver):
        this(definisjon.kode(), mutableListOf(definisjon), LocalDateTime.now()) {
            register(*observere)
            this.observers.varselkodeOpprettet(this, kode, gjeldendeDefinisjon)
        }

    init {
        require(kode.matches(mønster)) { "Ugyldig varselkode-format" }
        require(definisjoner.isNotEmpty()) { "Må ha minst én definisjon" }
    }

    @Transient
    private val observers: MutableSet<IVarselkodeObserver> = mutableSetOf()

    private val oppdelt = kode.split("_")
    private val domene = oppdelt[0]
    private val kontekst = oppdelt[1]
    private val nummer = oppdelt[2].toInt()
    private val gjeldendeDefinisjon get() = definisjoner.last()
    private val avviklet = gjeldendeDefinisjon.erAvviklet()

    internal fun register(vararg observere: IVarselkodeObserver) {
        this.observers.addAll(observere)
    }

    internal fun håndter(varseldefinisjon: Varseldefinisjon) {
        if (varseldefinisjon == gjeldendeDefinisjon) throw VarselException.IngenEndring(varseldefinisjon)
        definisjoner.add(varseldefinisjon)
        observers.varselkodeEndret(this, kode, gjeldendeDefinisjon)
    }

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

        internal fun Set<Varselkode>.gjeldendeDefinisjoner(): List<Varseldefinisjon> {
            return sortedBy { it.kode }.map { it.gjeldendeDefinisjon }
        }
    }

    override fun toString(): String {
        return "$kode, antall definisjoner=${definisjoner.size}"
    }

    override fun equals(other: Any?) = this === other || (
        other is Varselkode
            && javaClass == other.javaClass
            && kode == other.kode
            && definisjoner == other.definisjoner
        )

    override fun hashCode(): Int {
        var result = kode.hashCode()
        result = 31 * result + definisjoner.hashCode()
        return result
    }


}