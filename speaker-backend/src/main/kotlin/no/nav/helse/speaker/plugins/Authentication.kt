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

                val groups: List<String> = claims.getAsList("groups")

                val hasValidClaims = azureAD.hasValidClaims(claims.allClaims.keys.toList())
                val hasValidGroup = azureAD.hasValidGroups(groups)
                val validToken = hasValidClaims && hasValidGroup

                sikkerlogg.info("Claims: ${claims.allClaims.entries.joinToString {(k, v) -> "($k: $v)" }}")

                if (!validToken) {
                    sikkerlogg.info(
                        "Har ikke gyldig token. {}, {}",
                        kv("harGyldigeClaims", hasValidClaims),
                        kv("harGyldigeGrupper", hasValidGroup),
                    )
                    sikkerlogg.info(
                        "Har følgende grupper: ${groups.joinToString()}"
                    )
                }

                validToken
            }
        )
    }
}