package no.nav.helse.speaker

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.IVarselkodeObserver
import no.nav.helse.speaker.domene.VarselRepository
import no.nav.helse.speaker.domene.Varseldefinisjon
import no.nav.helse.speaker.domene.Varselkode
import org.slf4j.LoggerFactory

internal class Mediator(private val rapidsConnection: RapidsConnection, private val varselRepository: VarselRepository): IVarselkodeObserver {
    internal fun håndterOppdatering(varseldefinisjon: Varseldefinisjon) {
        val varselkode = varselRepository.finn(varseldefinisjon.kode()) ?: throw VarselException.FinnesIkke(varseldefinisjon)
        logg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
        varselkode.register(this)
        varselkode.håndter(varseldefinisjon)
        varselRepository.oppdater(varselkode)
    }

    internal fun håndterOpprettet(varseldefinisjon: Varseldefinisjon) {
        if (varselRepository.finn(varseldefinisjon.kode()) != null) throw VarselException.FinnesAllerede(varseldefinisjon)
        logg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
        val varselkode = Varselkode(varseldefinisjon, this)
        varselRepository.oppdater(varselkode)
    }

    internal fun finnGjeldendeVarseldefinisjoner(): List<Varseldefinisjon> {
        logg.info("Slår opp varseldefinisjoner")
        return varselRepository.finnGjeldendeDefinisjoner()
    }

    internal fun finnNesteVarselkode(subdomene: String?, kontekst: String?): String {
        val mønsterSomMåMatche = "^\\D{2}$".toRegex()
        if (subdomene == null || !subdomene.matches(mønsterSomMåMatche)) {
            throw VarselException.UgyldigSubdomene(subdomene)
        }

        if (kontekst == null || !kontekst.matches(mønsterSomMåMatche)) {
            throw VarselException.UgyldigKontekst(kontekst)
        }

        logg.info("Finner neste varselkode for {}, {}", kv("subdomene", subdomene), kv("kontekst", kontekst))

        val prefix = "${subdomene}_${kontekst}"
        return varselRepository.finnNesteVarselkodeFor(prefix)
    }

    override fun varselkodeOppdatert(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
        logg.info("Publiserer oppdatert definisjon for {}", kv("varselkode", kode))
        rapidsConnection.publish(
            JsonMessage.newMessage(
                "varselkode_ny_definisjon",
                mapOf(
                    "varselkode" to kode,
                    "gjeldende_definisjon" to gjeldendeDefinisjon.toMap()
                )
            ).toJson()
        )
    }

    private companion object {
        private val logg = LoggerFactory.getLogger(Mediator::class.java)
    }
}
