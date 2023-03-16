package no.nav.helse.speaker.domene

import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import no.nav.helse.speaker.domene.Varselkode.Companion.gjeldendeDefinisjoner

internal interface VarselRepository: IVarselkodeObserver {
    fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon>
    fun finnNesteVarselkodeFor(prefix: String): String
    fun finnSubdomenerOgKontekster(): Set<Subdomene>
    fun finn(varselkode: String): Varselkode?
}

internal class ActualVarselRepository(private val dao: VarseldefinisjonDao): VarselRepository {
    override fun finn(varselkode: String): Varselkode? {
        return dao.finn(varselkode)
    }

    override fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon> {
        return dao.finnVarselkoder().gjeldendeDefinisjoner()
    }

    override fun finnNesteVarselkodeFor(prefix: String): String {
        val koder = dao.finnVarselkoder()
        return koder.finnNeste(prefix)
    }

    override fun finnSubdomenerOgKontekster(): Set<Subdomene> {
        return dao.finnSubdomener()
    }

    override fun varselkodeEndret(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
        dao.oppdater(varselkode)
    }

    override fun nyVarselkode(varselkode: Varselkode, kode: String, gjeldendeDefinisjon: Varseldefinisjon) {
        dao.opprett(varselkode)
    }

    override fun nyttSubdomene(navn: String, forkortelse: String) {
        dao.opprettSubdomene(navn, forkortelse)
    }
}