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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import no.nav.helse.speaker.IMsGraphClient
import no.nav.helse.speaker.domene.Bruker
import org.slf4j.LoggerFactory
import java.util.*

internal class MsGraphClient(
    private val azureAD: AzureAD,
    private val groupId: String,
    private val graphUrl: String
): IMsGraphClient {
    private val httpClient: HttpClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json()
        }
    }

    companion object {
        private val logg = LoggerFactory.getLogger("tjenestekall")
    }

    override fun finnTeammedlemmer(): List<Bruker> {
        logg.info("Henter teammedlemmer")
        val token = runBlocking { azureAD.fetchToken() }
        val response = runBlocking {
            httpClient.get("$graphUrl/groups/$groupId/members?\$select=id,displayName,mail,onPremisesSamAccountName") {
                bearerAuth(token.toString())
                accept(ContentType.parse("application/json"))
            }.body<JsonObject>()
        }
        val brukere = response.getValue("value").jsonArray.map { userNode ->
            val userObject = userNode.jsonObject
            Bruker(
                epostadresse = userObject["mail"].toString(),
                navn = userObject["displayName"].toString(),
                ident = userObject["onPremisesSamAccountName"].toString(),
                oid = UUID.fromString(userObject["id"].toString())
            )
        }
        logg.info("Teammedlemmer hentet")
        return brukere
    }
}