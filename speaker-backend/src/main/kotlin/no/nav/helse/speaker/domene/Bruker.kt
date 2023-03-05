package no.nav.helse.speaker.domene

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import kotlinx.serialization.Serializable
import no.nav.security.token.support.v2.TokenValidationContextPrincipal

@Serializable
class Bruker(
    private val epostadresse: String,
    private val navn: String,
    private val ident: String
) {
    internal companion object {
        internal fun fromCall(issuer: String, call: ApplicationCall): Bruker {
            return Bruker(
                epostadresse = call.getClaim(issuer, "preferred_username")
                    ?: throw IllegalStateException("Forventer å finne preferred_username"),
                navn = call.getClaim(issuer, "name") ?: throw IllegalStateException("Forventer å finne name"),
                ident = call.getClaim(issuer, "NAVident") ?: throw IllegalStateException("Forventer å finne NAVident")
            )
        }

        private fun ApplicationCall.getClaim(issuer: String, name: String): String? =
            this.authentication.principal<TokenValidationContextPrincipal>()
                ?.context
                ?.getClaims(issuer)
                ?.getStringClaim(name)
    }

    override fun equals(other: Any?) = this === other || (
        other is Bruker &&
            javaClass == other.javaClass &&
            epostadresse == other.epostadresse &&
            navn == other.navn &&
            ident == other.ident
        )

    override fun hashCode(): Int {
        var result = epostadresse.hashCode()
        result = 31 * result + navn.hashCode()
        result = 31 * result + ident.hashCode()
        return result
    }


}