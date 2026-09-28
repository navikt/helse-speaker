package no.nav.helse.speaker

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.bearerAuth
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal val jsonReader =
    Json {
        this.serializersModule =
            SerializersModule {
                this.contextual(LocalDateTimeSerializer())
                this.contextual(UUIDSerializer())
                this.contextual(OffsetDateTimeSerializer())
            }
        ignoreUnknownKeys = true
    }

internal suspend fun sanityVarselendringerListener(
    iProduksjonsmiljø: Boolean,
    sanityProjectId: String,
    sanityDataSet: String,
    sanityReadDatasetsToken: String,
    sender: Sender,
    bøtte: Bøtte
) {
    val client =
        HttpClient(CIO) {
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 120_000
            }
            install(SSE) {
                showCommentEvents()
                showRetryEvents()
                maxReconnectionAttempts = 5
                reconnectionTime = 5.seconds
            }
            install(ContentNegotiation) {
                json()
            }
        }
    try {
        keepListening {
            client.sse(
                urlString = """https://$sanityProjectId.api.sanity.io/v2026-05-19/data/listen/$sanityDataSet""",
                request = {
                    url {
                        parameters.append("query", """*[_type == "varsel" && !(_id in path("drafts.**"))]""")
                        parameters.append("includeResult", "true")
                        bearerAuth(sanityReadDatasetsToken)
                        val lastEventId = bøtte.hentLastEventId()
                        if (lastEventId != null) {
                            logg.info("Bruker lastEventId i kall: $lastEventId")
                            parameters.append("lastEventId", lastEventId)
                        }
                    }
                },
            ) {
                logg.info("Etablerer lytter mot Sanity")
                incoming.collect { event ->
                    val data = event.data ?: return@collect // Sanity sender også meldinger uten data.
                    if (erVelkomsthilsen(data)) return@collect
                    logg.info("Mottatt melding fra Sanity")
                    try {
                        val (id, melding) =
                            jsonReader
                                .decodeFromString<SanityEndring>(data)
                        logg.info("Mottatt varseldefinisjon: $data")
                        melding.forsøkPubliserDefinisjon(iProduksjonsmiljø, sender)
                        bøtte.lagreLastEventId(id)
                    } catch (_: SerializationException) {
                        logg.info("Meldingen er ikke en varseldefinisjon. $data")
                    }
                }
            }
        }
    } finally {
        logg.info("Client lukkes")
        client.close()
    }
}

internal suspend fun keepListening(delayBeforeReconnect: Duration = 5.seconds, listen: suspend () -> Unit) {
    while (currentCoroutineContext().isActive) {
        try {
            listen()
            logg.warn("Sanity-lytteren ble avsluttet uten feil. Kobler til på nytt")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logg.error("Sanity-lytteren feilet. Kobler til på nytt", e)
        }
        delay(delayBeforeReconnect)
    }
}

private fun erVelkomsthilsen(data: String) = try {
    val message = Json.decodeFromString<Velkomsthilsen>(data)
    logg.info("Mottatt velkomsthilsen: {}", message)
    true
} catch (_: Exception) {
    false
}

internal fun Varseldefinisjon.forsøkPubliserDefinisjon(
    iProduksjonsmiljø: Boolean,
    sender: Sender,
) {
    if (iProduksjonsmiljø && !this.iProduksjon) {
        logg.info("I produksjonsmiljø og meldingen er markert \"ikke i produksjon\". Publiserer ikke varseldefinisjon på rapiden")
        return
    }
    sender.send(this@forsøkPubliserDefinisjon.toUtgåendeMelding())
}

@Serializable
data class Velkomsthilsen(
    val listenerName: String,
)

@Serializable
data class SanityEndring(
    val eventId: String,
    val result: Varseldefinisjon,
)

@Serializable
data class VarseldefinisjonEvent(
    @SerialName("@event_name")
    val eventName: String,
    val varselkode: String,
    @SerialName("gjeldende_definisjon")
    val gjeldendeDefinisjon: UtgåendeVarseldefinisjon,
)

@Serializable
data class UtgåendeVarseldefinisjon(
    @Contextual
    val id: UUID,
    val tittel: String,
    val avviklet: Boolean,
    val forklaring: String? = null,
    val handling: String? = null,
    @Contextual
    val opprettet: LocalDateTime,
    val kode: String,
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
    val _updatedAt: OffsetDateTime,
    val varselkode: String,
) {
    fun toUtgåendeMelding(): VarseldefinisjonEvent =
        VarseldefinisjonEvent(
            eventName = "varselkode_ny_definisjon",
            varselkode = varselkode,
            gjeldendeDefinisjon =
                UtgåendeVarseldefinisjon(
                    id = UUID.nameUUIDFromBytes("$_id$_rev".toByteArray()),
                    kode = varselkode,
                    tittel = tittel,
                    avviklet = avviklet,
                    forklaring = forklaring,
                    handling = handling,
                    // Konverter UTC timestamp fra Sanity til LocalDateTime
                    opprettet = _updatedAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                ),
        )
}
