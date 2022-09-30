package no.nav.helse.speaker

internal interface VarselRepository {
    fun nytt(varsel: Varsel): Boolean
    fun nytt(kode: String, tittel: String, forklaring: String?, handling: String?): Boolean
    fun finn(): List<Varsel>
    fun oppdater(kode: String)
}