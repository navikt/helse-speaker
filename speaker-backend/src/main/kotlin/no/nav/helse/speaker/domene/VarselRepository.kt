package no.nav.helse.speaker.domene

import no.nav.helse.speaker.db.VarseldefinisjonDao
import no.nav.helse.speaker.domene.Varselkode.Companion.finnNeste
import no.nav.helse.speaker.domene.Varselkode.Companion.finnSubdomenerOgKontekster
import no.nav.helse.speaker.domene.Varselkode.Companion.gjeldendeDefinisjoner

internal interface VarselRepository {
    fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon>
    fun oppdater(varselkode: Varselkode)
    fun finnNesteVarselkodeFor(prefix: String): String
    fun finnSubdomenerOgKontekster(): Map<String, Set<String>>
    fun finn(varselkode: String): Varselkode?
    fun opprett(varselkode: Varselkode)
}

internal class ActualVarselRepository(private val dao: VarseldefinisjonDao): VarselRepository {
    override fun finn(varselkode: String): Varselkode? {
        return dao.finn(varselkode)
    }

    override fun opprett(varselkode: Varselkode) {
        return dao.opprett(varselkode)
    }
    override fun finnGjeldendeDefinisjoner(): List<Varseldefinisjon> {
        return dao.finnVarselkoder().gjeldendeDefinisjoner()
    }

    override fun finnNesteVarselkodeFor(prefix: String): String {
        val koder = dao.finnVarselkoder()
        return koder.finnNeste(prefix)
    }

    override fun finnSubdomenerOgKontekster(): Map<String, Set<String>> {
        val koder = dao.finnVarselkoder()
        return koder.finnSubdomenerOgKontekster()
    }

    override fun oppdater(varselkode: Varselkode) {
        dao.oppdater(varselkode)
    }
}