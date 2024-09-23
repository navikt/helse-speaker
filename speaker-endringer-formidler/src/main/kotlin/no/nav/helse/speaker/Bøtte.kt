package no.nav.helse.speaker

import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

interface Bøtte {
    fun hentLastEventId(): String? = null
    fun lagreLastEventId(lastEventId: String): Boolean { return false }
}

class GCPBøtte(private val bøttenavn: String) : Bøtte {
    companion object {
        private val logger = LoggerFactory.getLogger("speaker-bøtte")
        private const val FILNAVN = "last_event_id.txt"
    }
    override fun hentLastEventId(): String = String(hentBøtte().get(FILNAVN).getContent())

    override fun lagreLastEventId(lastEventId: String): Boolean {
        logger.info("Lagrer last event id i bøtta")
        return lagre(lastEventId, FILNAVN)
    }

    private fun lagre(tekst: String, filnavn: String): Boolean {
        val bøtte = hentBøtte()
        val blob = bøtte.get(filnavn)
        if (blob == null) {
            bøtte.create(filnavn, tekst.encodeToByteArray(), "text/plain")
        } else {
            val writer = blob.writer()
            writer.write(ByteBuffer.wrap(tekst.encodeToByteArray()))
            writer.close()
        }
        return true
    }

    private fun hentBøtte(): Bucket {
        val storage = StorageOptions.getDefaultInstance().service
        return storage.get(bøttenavn) ?: error("Fant ikke bøtta som heter $bøttenavn")
    }
}
