package no.nav.helse.speaker

import no.nav.helse.speaker.db.VarselDao

internal interface VarselRepository {
    fun nytt(varsel: Varsel): Boolean
    fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean
    fun finn(): List<Varsel>
    fun oppdater(kode: String)
}

internal class ActualVarselRepository(private val dao: VarselDao): VarselRepository {
    override fun nytt(varsel: Varsel): Boolean {
        TODO("Not yet implemented")
    }

    override fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun finn(): List<Varsel> {
        return dao.finnVarsler()
    }

    override fun oppdater(kode: String) {
        TODO("Not yet implemented")
    }

}