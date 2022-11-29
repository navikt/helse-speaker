package no.nav.helse.speaker

import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.db.VarselException

internal interface VarselRepository {
    fun nytt(varseldefinisjon: Varseldefinisjon): Boolean
    fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean
    fun finn(): List<Varseldefinisjon>
    fun finnGjeldendeDefinisjonFor(varselkode: String): Varseldefinisjon
    fun oppdater(varseldefinisjon: Varseldefinisjon)
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
    override fun oppdater(varseldefinisjon: Varseldefinisjon) {
        val gjeldendeDefinisjon = varseldefinisjon.finnGjeldendeDefinisjon(this)
        val definisjonOppdatert = gjeldendeDefinisjon.oppdaterDefinisjon(varseldefinisjon, dao::oppdaterDefinisjon)
        val avvikletOppdatert = gjeldendeDefinisjon.oppdaterAvviklet(varseldefinisjon, dao::oppdaterAvviklet)
        if (!definisjonOppdatert && !avvikletOppdatert) throw VarselException.IngenEndring(varseldefinisjon)
    }

    override fun finnGjeldendeDefinisjonFor(varselkode: String): Varseldefinisjon {
        return dao.finnSisteDefinisjonFor(varselkode) ?: throw VarselException.DefinisjonFinnesIkke(varselkode)
    }
}