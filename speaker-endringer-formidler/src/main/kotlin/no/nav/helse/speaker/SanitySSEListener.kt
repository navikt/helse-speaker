package no.nav.helse.speaker

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

private val jsonReader =
    Json {
        this.serializersModule =
            SerializersModule {
                this.contextual(LocalDateTimeSerializer())
                this.contextual(UUIDSerializer())
            }
        ignoreUnknownKeys = true
    }

internal suspend fun sanityVarselendringerListener(
    iProduksjonsmiljø: Boolean,
    sanityProjectId: String,
    sanityDataSet: String,
    rapidsConnection: RapidsConnection,
) {
    val client =
        HttpClient {
            install(SSE) {
                showCommentEvents()
                showRetryEvents()
            }
            install(ContentNegotiation) {
                json()
            }
        }
    client.sse(
        urlString = """https://$sanityProjectId.api.sanity.io/v2021-06-07/data/listen/$sanityDataSet""",
        request = {
            url {
                parameters.append("query", """*[_type == "varsel"]""")
                parameters.append("includeResult", "true")
            }
        },
        showCommentEvents = false,
        showRetryEvents = false,
        reconnectionTime = 5000.milliseconds,
    ) {
        while (true) {
            incoming.collect { event ->
                val data = event.data ?: return@collect
                sikkerlogg.info("Mottatt melding fra Sanity")
                try {
                    jsonReader.decodeFromString<SanityEndring>(data).result
                        .forsøkPubliserDefinisjon(iProduksjonsmiljø, rapidsConnection)
                    sikkerlogg.info("Mottatt varseldefinisjon: $data")
                } catch (_: SerializationException) {
                }
            }
        }
    }
}

internal fun Varseldefinisjon.forsøkPubliserDefinisjon(
    iProduksjonsmiljø: Boolean,
    rapidsConnection: RapidsConnection,
) {
    if (iProduksjonsmiljø && !this.iProduksjon) return
    rapidsConnection.publish(
        JsonMessage.newMessage(
            eventName = "varselkode_ny_definisjon",
            map =
                mapOf(
                    "varselkode" to this.varselkode,
                    "gjeldende_definisjon" to this.toKafkamelding(),
                ),
        ).toJson(),
    )
}

@Serializable
data class SanityEndring(
    val result: Varseldefinisjon,
)

@Serializable
data class Varseldefinisjon(
    @Contextual
    val _id: UUID,
    val _rev: String,
    val tittel: String,
    val avviklet: Boolean,
    val forklaring: String? = null,
    val handling: String? = null,
    val iProduksjon: Boolean,
    @Contextual
    val _updatedAt: LocalDateTime,
    val varselkode: String,
) {
    fun toKafkamelding(): Map<String, Any> =
        mutableMapOf(
            "id" to UUID.nameUUIDFromBytes("$_id$_rev".toByteArray()),
            "kode" to varselkode,
            "tittel" to tittel,
            "avviklet" to avviklet,
            "opprettet" to _updatedAt,
        ).apply {
            compute("forklaring") { _, _ -> forklaring }
            compute("handling") { _, _ -> handling }
        }.toMap()
}
