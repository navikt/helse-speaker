package no.nav.helse.speaker.domene

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import no.nav.helse.speaker.domene.Varselkode.Companion.finnSubdomenerOgKontekster
import org.slf4j.LoggerFactory

internal interface VarselRepository {
    fun ny(varseldefinisjon: Varseldefinisjon): Boolean
    fun ny(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean
    fun finn(): List<Varseldefinisjon>
    fun finnGjeldendeDefinisjonFor(varselkode: String): Varseldefinisjon
    fun oppdater(varseldefinisjon: Varseldefinisjon)
    fun finnNesteVarselkodeFor(prefix: String): String
    fun finnSubdomenerOgKontekster(): Map<String, Set<String>>
}

internal class ActualVarselRepository(private val dao: VarseldefinisjonDao): VarselRepository {
    override fun ny(varseldefinisjon: Varseldefinisjon): Boolean {
        val siste = dao.finnSisteDefinisjonFor(varseldefinisjon.kode())
        if (siste != null) throw VarselException.KodeEksisterer(varseldefinisjon.kode())
        return varseldefinisjon.lagre(this)
    }
    override fun ny(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean {
        return dao.nyDefinisjon(kode, tittel, forklaring, handling, false)
    }
    override fun finn(): List<Varseldefinisjon> {
        return dao.finnDefinisjoner()
    }

    override fun finnNesteVarselkodeFor(prefix: String): String {
        val koder = dao.finnVarselkoder()
        return koder.finnNeste(prefix)
    }

    override fun finnSubdomenerOgKontekster(): Map<String, Set<String>> {
        val koder = dao.finnVarselkoder()
        return koder.finnSubdomenerOgKontekster()
    }

    override fun oppdater(varseldefinisjon: Varseldefinisjon) {
        val gjeldendeDefinisjon = varseldefinisjon.finnGjeldendeDefinisjon(this)
        val definisjonOppdatert = gjeldendeDefinisjon.oppdaterDefinisjon(varseldefinisjon, dao::oppdaterDefinisjon)
        val avvikletOppdatert = gjeldendeDefinisjon.oppdaterAvviklet(varseldefinisjon, dao::oppdaterAvviklet)
        if (!definisjonOppdatert && !avvikletOppdatert) throw VarselException.IngenEndring(varseldefinisjon)
        val oppdatering = buildString {
            if (definisjonOppdatert) append("definisjon")
            if (avvikletOppdatert && definisjonOppdatert) append(" og ")
            if (avvikletOppdatert) append("avvikletstatus")
        }
        logg.info(
            "Oppdatert $oppdatering for {}",
            kv("varselkode", varseldefinisjon.kode()),
        )
        sikkerlogg.info(
            "Oppdatert $oppdatering for {}\n{}",
            kv("varselkode", varseldefinisjon.kode()),
            kv("ny definisjon", varseldefinisjon)
        )
    }

    override fun finnGjeldendeDefinisjonFor(varselkode: String): Varseldefinisjon {
        return dao.finnSisteDefinisjonFor(varselkode) ?: throw VarselException.DefinisjonFinnesIkke(varselkode)
    }
    private companion object {
        private val logg = LoggerFactory.getLogger(ActualVarselRepository::class.java)
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
}