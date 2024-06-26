package no.nav.helse.speaker

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

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
            incoming
                .retry(5)
                .catch {
                    logg.error("Feil ved lesing av flow: {}", it.stackTraceToString())
                    throw it
                }.collect { event ->
                    val data = event.data ?: return@collect
                    logg.info("Mottatt melding fra Sanity")
                    try {
                        val melding =
                            jsonReader
                                .decodeFromString<SanityEndring>(data)
                                .result
                        logg.info("Mottatt varseldefinisjon: $data")
                        melding.forsøkPubliserDefinisjon(iProduksjonsmiljø, sender)
                    } catch (_: SerializationException) {
                        logg.info("Meldingen er ikke en varseldefinisjon. Se sikkerlogg for data i meldingen.")
                        sikkerlogg.info("Meldingen er ikke en varseldefinisjon: $data")
                    }
                }
        }
    }
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
data class SanityEndring(
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
