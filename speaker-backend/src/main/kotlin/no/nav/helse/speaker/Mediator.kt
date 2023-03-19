package no.nav.helse.speaker

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.speaker.domene.*
import no.nav.helse.speaker.domene.Subdomene.Companion.finnesAllerede
import no.nav.helse.speaker.domene.Subdomene.Companion.håndterNyKontekst
import no.nav.helse.speaker.domene.Subdomene.Companion.register
import org.slf4j.LoggerFactory

internal class Mediator(
    rapidsConnection: () -> RapidsConnection,
    private val varselRepository: VarselRepository,
    private val msGraphClient: IMsGraphClient
): IVarselkodeObserver {
    private val rapidsConnection: RapidsConnection by lazy { rapidsConnection() }

    internal fun håndterOppdatertVarselkode(varseldefinisjon: Varseldefinisjon) {
        val varselkode = varselRepository.finn(varseldefinisjon.kode()) ?: throw VarselException.FinnesIkke(varseldefinisjon)
        sikkerlogg.info("Oppdaterer {}", kv("varselkode", varseldefinisjon.kode()))
        varselkode.register(this, varselRepository)
        varselkode.håndter(varseldefinisjon)
    }

    internal fun håndterNyVarselkode(varseldefinisjon: Varseldefinisjon) {
        if (varselRepository.finn(varseldefinisjon.kode()) != null) throw VarselException.FinnesAllerede(varseldefinisjon)
        sikkerlogg.info("Oppretter {}", kv("varselkode", varseldefinisjon.kode()))
        Varselkode(varseldefinisjon, this, varselRepository)
    }

    internal fun håndterNyttSubdomene(subdomene: Subdomene) {
        subdomene.register(this, varselRepository)
        val eksisterendeSubdomener = varselRepository.finnSubdomenerOgKontekster()
        if (eksisterendeSubdomener.finnesAllerede(subdomene)) throw VarselException.SubdomeneFinnesAllerede(subdomene)
        subdomene.opprett()
    }

    internal fun håndterNyKontekst(kontekstPayload: KontekstPayload) {
        val eksisterendeSubdomener = varselRepository.finnSubdomenerOgKontekster()
        eksisterendeSubdomener.register(this, varselRepository)
        eksisterendeSubdomener.håndterNyKontekst(kontekstPayload)
    }

    internal fun finnTeammedlemmer(): List<Bruker> {
        return msGraphClient.finnTeammedlemmer()
    }

    internal fun finnGjeldendeVarseldefinisjoner(): List<Varseldefinisjon> {
        sikkerlogg.info("Slår opp varseldefinisjoner")
        return varselRepository.finnGjeldendeDefinisjoner()
    }

    internal fun finnSubdomenerOgKontekster(): Set<Subdomene> {
        sikkerlogg.info("Slår opp subdomener og kontekster")
        return varselRepository.finnSubdomenerOgKontekster()
    }

    internal fun finnNesteVarselkode(subdomene: String?, kontekst: String?): String {
        val mønsterSomMåMatche = "^\\D{2}$".toRegex()
        if (subdomene == null || !subdomene.matches(mønsterSomMåMatche)) {
            throw VarselException.UgyldigSubdomene(subdomene)
        }

        if (kontekst == null || !kontekst.matches(mønsterSomMåMatche)) {
            throw VarselException.UgyldigKontekst(kontekst)
        }

        sikkerlogg.info("Finner neste varselkode for {}, {}", kv("subdomene", subdomene), kv("kontekst", kontekst))

        val prefix = "${subdomene}_${kontekst}"
        return varselRepository.finnNesteVarselkodeFor(prefix)
    }

    override fun varselkodeEndret(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
        sikkerlogg.info("Publiserer oppdatert definisjon for {}", kv("varselkode", kode))
        publiser(kode, gjeldendeDefinisjon)
    }

    override fun nyVarselkode(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
        sikkerlogg.info("Publiserer opprettet definisjon for {}", kv("varselkode", kode))
        publiser(kode, gjeldendeDefinisjon)
    }

    private fun publiser(kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
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
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
}
