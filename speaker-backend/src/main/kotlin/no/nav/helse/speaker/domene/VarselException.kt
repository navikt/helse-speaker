package no.nav.helse.speaker.domene

import io.ktor.http.HttpStatusCode

internal sealed class VarselException(message: String) : Exception(message) {
    internal abstract val httpStatusCode: HttpStatusCode

    internal class IngenEndring(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varseldefinisjon $varseldefinisjon er allerede gjeldende definisjon") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotModified
    }

    internal class FinnesIkke(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varselkode for varseldefinisjon $varseldefinisjon finnes ikke") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.NotFound
    }

    internal class FinnesAllerede(varseldefinisjon: Varseldefinisjon) :
        VarselException("Varselkode for varseldefinisjon $varseldefinisjon finnes fra før av") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.Conflict
    }

    internal class UgyldigSubdomene(subdomene: String?):
        VarselException("Subdomene=$subdomene er ikke på gyldig format.") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }

    internal class UgyldigKontekst(kontekst: String?):
        VarselException("Kontekst=$kontekst er ikke på gyldig format.") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest
    }

    internal class SubdomeneFinnesAllerede(subdomene: Subdomene) : VarselException("Subdomene=${subdomene} finnes allerede.") {
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.Conflict
    }
}