package no.nav.helse.speaker

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
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
import kotlin.time.Duration.Companion.milliseconds
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
    sender: Sender,
    bøtte: Bøtte
) {
    val client =
        HttpClient(CIO) {
            engine {
                endpoint {
                    this.connectAttempts = Int.MAX_VALUE
                }
            }
            install(SSE) {
                showCommentEvents()
                showRetryEvents()
                reconnectionTime = 5000.milliseconds
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
                val lastEventId = bøtte.hentLastEventId()
                if (lastEventId != null) {
                    logg.info("Bruker lastEventId i kall: $lastEventId")
                    parameters.append("lastEventId", lastEventId)
                }
            }
        },
        showCommentEvents = false,
        showRetryEvents = false,
        reconnectionTime = 5000.milliseconds,
    ) {
        logg.info("Etablerer lytter mot Sanity")
        incoming
            .retry(5) {
                logg.info("Feil oppsto i flow: ${it.localizedMessage}", it)
                delay(15.seconds)
                true
            }
            .catch {
                logg.error("Feil ved lesing av flow: {}", it.localizedMessage, it)
                throw it
            }.collect { event ->
                val data = event.data ?: return@collect
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
    logg.info("Client lukkes")
    client.close()
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
