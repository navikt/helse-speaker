package no.nav.helse.speaker

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.time.LocalDateTime
import java.util.*

abstract class Sender(
    protected val instance: String,
    protected val image: String,
) {
    protected abstract fun send(melding: String)

    internal fun send(melding: VarseldefinisjonEvent) {
        val json =
            jsonReader.encodeToJsonElement(melding).jsonObject +
                buildJsonObject {
                    val id = UUID.randomUUID()
                    val tidspunkt = LocalDateTime.now()
                    put("system_participating_services", systemParticipatingServices(id, tidspunkt))
                    put("@id", "$id")
                    put("@opprettet", "$tidspunkt")
                }
        val utgåendeMelding = Json.encodeToString(json)
        logg.info("Sender melding som følge av endret varseldefinisjon: {}", utgåendeMelding)
        send(utgåendeMelding)
    }

    private fun systemParticipatingServices(
        id: UUID,
        tidspunkt: LocalDateTime,
    ) = buildJsonArray {
        this.addJsonObject {
            put("image", image)
            put("service", "speaker")
            put("id", "$id")
            put("time", "$tidspunkt")
            put("instance", instance)
        }
    }
}
