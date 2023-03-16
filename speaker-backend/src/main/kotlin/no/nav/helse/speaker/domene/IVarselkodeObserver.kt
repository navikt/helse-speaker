package no.nav.helse.speaker.domene

internal interface IVarselkodeObserver {
    fun varselkodeEndret(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {}
    fun nyVarselkode(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {}
    fun nyttSubdomene(navn: String, forkortelse: String) {}

    companion object {
        internal fun Set<IVarselkodeObserver>.varselkodeEndret(
            varselkode: Varselkode,
            kode: String,
            gjeldendeDefinisjon: Varseldefinisjon
        ) {
            forEach { it.varselkodeEndret(varselkode, kode, gjeldendeDefinisjon) }
        }
        internal fun Set<IVarselkodeObserver>.varselkodeOpprettet(
            varselkode: Varselkode,
            kode: String,
            gjeldendeDefinisjon: Varseldefinisjon
        ) {
            forEach { it.nyVarselkode(varselkode, kode, gjeldendeDefinisjon) }
        }
        internal fun Set<IVarselkodeObserver>.nyttSubdomene(
            navn: String,
            forkortelse: String
        ) {
            forEach { it.nyttSubdomene(navn, forkortelse) }
        }
    }
}