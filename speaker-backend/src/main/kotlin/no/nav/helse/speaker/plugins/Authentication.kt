package no.nav.helse.speaker.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.speaker.azure.AzureAD
import no.nav.security.token.support.v2.TokenSupportConfig
import no.nav.security.token.support.v2.tokenValidationSupport
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal fun Application.configureAuthentication(azureAD: AzureAD) {
    install(Authentication) {
        tokenValidationSupport(
            name = "ValidToken",
            config = TokenSupportConfig(
                azureAD.issuerConfig(),
            ),
            additionalValidation = { ctx ->
                val claims = ctx.getClaims(azureAD.issuer())
                val scopes = claims
                    ?.getStringClaim("scope")
                    ?.split(" ")
                    ?: emptyList()

                val groups: List<String> = claims.getAsList("groups")

                val hasValidClaims = azureAD.hasValidClaims(claims.allClaims.keys.toList())
                val hasValidScopes = azureAD.hasValidScopes(scopes)
                val hasValidGroup = azureAD.hasValidGroups(groups)
                val validToken = hasValidClaims && hasValidScopes && hasValidGroup

                if (!validToken) {
                    sikkerlogg.info(
                        "Har ikke gyldig token. {}, {}, {}",
                        kv("harGyldigeClaims", hasValidClaims),
                        kv("harGyldigeScopes", hasValidScopes),
                        kv("harGyldigeGrupper", hasValidGroup),
                    )
                }

                validToken
            }
        )
    }
}