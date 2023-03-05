package no.nav.helse.speaker.domene

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.db.VarselException
import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import org.slf4j.LoggerFactory

internal interface VarselRepository {
    fun nytt(varseldefinisjon: Varseldefinisjon): Boolean
    fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean
    fun finn(): List<Varseldefinisjon>
    fun finnGjeldendeDefinisjonFor(varselkode: String): Varseldefinisjon
    fun oppdater(varseldefinisjon: Varseldefinisjon)
    fun finnNesteVarselkodeFor(prefix: String): String
}

internal class ActualVarselRepository(private val dao: VarseldefinisjonDao): VarselRepository {
    override fun nytt(varseldefinisjon: Varseldefinisjon): Boolean {
        TODO("Not yet implemented")
    }
    override fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean {
        TODO("Not yet implemented")
    }
    override fun finn(): List<Varseldefinisjon> {
        return dao.finnDefinisjoner()
    }

    override fun finnNesteVarselkodeFor(prefix: String): String {
        val koder = dao.finnVarselkoder()
        return koder.finnNeste(prefix)
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