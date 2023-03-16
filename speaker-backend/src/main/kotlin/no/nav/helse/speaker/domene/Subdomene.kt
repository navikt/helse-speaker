package no.nav.helse.speaker.domene

import kotlinx.serialization.Serializable
import no.nav.helse.speaker.domene.IVarselkodeObserver.Companion.nyttSubdomene

@Serializable
internal data class Subdomene(
    private val navn: String,
    private val forkortelse: String,
    private val kontekster: Set<Kontekst>
) {
    internal constructor(
        navn: String,
        forkortelse: String
    ): this(navn, forkortelse, emptySet())

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

    internal companion object {
        internal fun Set<Subdomene>.finnesAllerede(subdomene: Subdomene): Boolean {
            val navn = map { it.navn }
            val forkortelser = map { it.forkortelse }
            return subdomene.navn in navn || subdomene.forkortelse in forkortelser
        }

        private val pattern = "^[A-ZÆØÅ]{2}$".toRegex()
    }
}

@Serializable
internal data class Kontekst(
    private val navn: String,
    private val forkortelse: String,
)

@Serializable
internal data class SubdomenePayload(
    private val navn: String,
    private val forkortelse: String
) {
    internal fun toSubdomene() = Subdomene(navn, forkortelse)
}