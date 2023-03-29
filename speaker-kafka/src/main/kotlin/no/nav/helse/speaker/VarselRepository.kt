package no.nav.helse.speaker

import no.nav.helse.speaker.db.VarselDao
import javax.sql.DataSource

internal interface VarselRepository {
    fun varselkoderFor(varselkoder: List<String>): List<Varselkode>
}

internal class ActualVarselRepository(dataSource: () -> DataSource) : VarselRepository {
    private val varselDao = VarselDao(dataSource())

    override fun varselkoderFor(varselkoder: List<String>): List<Varselkode> {
        return varselDao.finnVarselkoderFor(varselkoder)
    }
}