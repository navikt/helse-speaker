package no.nav.helse.speaker.db

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class VarselDao(private val dataSource: DataSource) {

    internal fun valider(kode: String): Boolean {
        return sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = "SELECT 1 FROM varselkode WHERE kode = ?"
            session.run(queryOf(query, kode).map { it.boolean(1) }.asSingle) ?: false
        }
    }
}