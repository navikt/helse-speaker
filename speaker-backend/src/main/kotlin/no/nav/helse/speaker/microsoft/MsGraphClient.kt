package no.nav.helse.speaker.microsoft

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.*
import no.nav.helse.speaker.IMsGraphClient
import no.nav.helse.speaker.domene.Bruker
import no.nav.helse.speaker.domene.TeammedlemmerException
import org.slf4j.LoggerFactory
import java.util.*

internal class MsGraphClient(
    private val azureAD: AzureAD,
    private val groupId: String,
    private val graphUrl: String
): IMsGraphClient {
    private val httpClient: HttpClient = HttpClient(Apache) {
        expectSuccess = true
        install(ContentNegotiation) {
            json()
        }
    }

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    override suspend fun finnTeammedlemmer(): List<Bruker> {
        sikkerlogg.info("Henter access token for å kunne hente teammedlemmer")
        val token = azureAD.fetchToken()
        sikkerlogg.info("Access token hentet, henter teammedlemmer")
        val response = try {
            httpClient.get("$graphUrl/groups/$groupId/members?\$select=id,givenName,surname,mail,onPremisesSamAccountName") {
                bearerAuth(token.toString())
                accept(ContentType.parse("application/json"))
            }.body<JsonObject>()
        } catch (e: Exception) {
            sikkerlogg.error("Klarte ikke hente teammedlemmer fra Graph: ${e.message}")
            throw TeammedlemmerException.HentingFeilet(e.message!!)
        }
        sikkerlogg.info("Respons fra Graph: $response")
        val brukere = response.getValue("value").jsonArray
            .filterNot { it.jsonObject["givenName"]?.jsonPrimitive?.contentOrNull == null }
            .map { userNode ->
            val userObject = userNode.jsonObject
            Bruker(
                epostadresse = userObject.getValue("mail").jsonPrimitive.content,
                navn = "${userObject.getValue("givenName").jsonPrimitive.content} ${userObject.getValue("surname").jsonPrimitive.content}",
                ident = userObject.getValue("onPremisesSamAccountName").jsonPrimitive.content,
                oid = UUID.fromString(userObject.getValue("id").jsonPrimitive.content)
            )
        }
        sikkerlogg.info("Teammedlemmer hentet")
        return brukere
    }
}