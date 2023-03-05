package no.nav.helse.speaker.domene

internal class Varselkode(kode: String) {
    init {
        require(kode.matches(mønster)) { "Ugyldig varselkode-format" }
    }
    private val oppdelt = kode.split("_")
    private val domene = oppdelt[0]
    private val kontekst = oppdelt[1]
    private val nummer = oppdelt[2].toInt()

    internal companion object {
        private val mønster = "^\\D{2}_\\D{2}_\\d{1,3}$".toRegex()
        internal fun List<Varselkode>.finnNeste(prefix: String): String {
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
    }
}