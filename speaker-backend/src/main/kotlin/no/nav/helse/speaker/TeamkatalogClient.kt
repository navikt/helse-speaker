package no.nav.helse.speaker

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import no.nav.helse.speaker.domene.Bruker
import java.util.UUID

internal class TeamkatalogClient(
    private val teamkatalogBaseUrl: String,
    private val teamId: UUID
): ITeamkatalogClient {
    private val httpClient: HttpClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json()
        }
    }

    override fun finnTeammedlemmer(): List<Bruker> {
        val response = runBlocking {
            httpClient.get("$teamkatalogBaseUrl/team") {
                parameter("clusterId", teamId)
                parameter("status", "ACTIVE")
            }.body<JsonObject>()
        }

        val brukere = response.getValue("content").jsonArray.flatMap { teamNode ->
            teamNode.jsonObject.getValue("members").jsonArray.map { userNode ->
                val resourceNode = userNode.jsonObject.getValue("resource").jsonObject
                Bruker(
                    epostadresse = resourceNode["email"].toString(),
                    navn = resourceNode["fullName"].toString(),
                    ident = resourceNode["navIdent"].toString()
                )
            }
        }
        return brukere
    }


}