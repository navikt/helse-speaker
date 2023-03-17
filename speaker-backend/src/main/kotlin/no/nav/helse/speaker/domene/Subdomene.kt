package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import no.nav.helse.speaker.domene.IVarselkodeObserver.Companion.nyKontekst
import no.nav.helse.speaker.domene.IVarselkodeObserver.Companion.nyttSubdomene
import no.nav.helse.speaker.domene.Kontekst.Companion.finnesAllerede

@Serializable
internal data class Subdomene private constructor(
    private val navn: String,
    private val forkortelse: String,
    private val kontekster: MutableSet<Kontekst>
) {
    internal constructor(
        navn: String,
        forkortelse: String
    ): this(navn, forkortelse, mutableSetOf())

    internal constructor(
        navn: String,
        forkortelse: String,
        kontekster: Collection<Kontekst>
    ): this(navn, forkortelse, kontekster.toMutableSet())

    init {
        if (!forkortelse.matches(pattern)) throw VarselException.UgyldigSubdomene(this.toString())
        if (navn.isBlank()) throw VarselException.UgyldigSubdomene(this.toString())
    }

    private val observere = mutableSetOf<IVarselkodeObserver>()

    override fun toString(): String {
        return "navn: $navn, forkortelse: $forkortelse"
    }

    internal fun register(vararg observere: IVarselkodeObserver) {
        this.observere.addAll(observere)
    }

    internal fun opprett() {
        observere.nyttSubdomene(navn, forkortelse)
    }

    internal fun håndterNyKontekst(kontekst: Kontekst) {
        if (kontekster.finnesAllerede(kontekst)) throw VarselException.KontekstFinnesAllerede(kontekst)
        kontekster.add(kontekst)
        kontekst.register(*observere.toTypedArray())
        kontekst.opprett(forkortelse)
    }

    internal companion object {
        private val pattern = "^[A-ZÆØÅ]{2}$".toRegex()

        internal fun Set<Subdomene>.finnesAllerede(subdomene: Subdomene): Boolean {
            val navn = map { it.navn }
            val forkortelser = map { it.forkortelse }
            return subdomene.navn in navn || subdomene.forkortelse in forkortelser
        }

        internal fun Set<Subdomene>.register(vararg observere: IVarselkodeObserver) {
            forEach { it.register(*observere) }
        }

        internal fun Set<Subdomene>.håndterNyKontekst(kontekstPayload: KontekstPayload) {
            val subdomene = find { kontekstPayload.tilhører(it.forkortelse) } ?: throw VarselException.SubdomeneFinnesIkke(kontekstPayload.toString())

            subdomene.håndterNyKontekst(kontekstPayload.toKontekst())
        }
    }
}

@Serializable
internal data class Kontekst(
    private val navn: String,
    private val forkortelse: String,
) {
    private val observere = mutableSetOf<IVarselkodeObserver>()
    init {
        if (!forkortelse.matches(pattern)) throw VarselException.UgyldigKontekst(this.toString())
        if (navn.isBlank()) throw VarselException.UgyldigKontekst(this.toString())
    }

    internal fun register(vararg observere: IVarselkodeObserver) {
        this.observere.addAll(observere)
    }

    internal fun opprett(subdomene: String) {
        observere.nyKontekst(navn, forkortelse, subdomene)
    }

    override fun toString() = "navn: $navn, forkortelse: $forkortelse"

    internal companion object {
        private val pattern = "^[A-ZÆØÅ]{2}$".toRegex()

        internal fun Set<Kontekst>.finnesAllerede(kontekst: Kontekst): Boolean {
            val navn = map { it.navn }
            val forkortelser = map { it.forkortelse }
            return kontekst.navn in navn || kontekst.forkortelse in forkortelser
        }
    }
}

@Serializable
internal data class SubdomenePayload(
    private val navn: String,
    private val forkortelse: String
) {
    internal fun toSubdomene() = Subdomene(navn, forkortelse)
}

@Serializable
internal data class KontekstPayload(
    private val navn: String,
    private val forkortelse: String,
    private val subdomene: String
) {
    internal fun tilhører(forkortelseSubdomene: String) = forkortelseSubdomene == subdomene
    internal fun toKontekst() = Kontekst(navn, forkortelse)
    override fun toString() = "navn: $navn, forkortelse: $forkortelse, subdomene: $subdomene"
}