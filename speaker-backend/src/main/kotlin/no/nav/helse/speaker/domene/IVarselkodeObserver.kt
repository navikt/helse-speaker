package no.nav.helse.speaker.domene

internal interface IVarselkodeObserver {
    fun varselkodeOppdatert(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon)

    companion object {
        internal fun List<IVarselkodeObserver>.varselkodeOppdatert(
            varselkode: Varselkode,
            kode: String,
            gjeldendeDefinisjon: Varseldefinisjon
        ) {
            forEach { it.varselkodeOppdatert(varselkode, kode, gjeldendeDefinisjon) }
        }
    }
}