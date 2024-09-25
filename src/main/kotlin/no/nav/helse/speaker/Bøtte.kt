package no.nav.helse.speaker

import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageOptions
import java.nio.ByteBuffer

interface Bøtte {
    fun hentLastEventId(): String? = null
    fun lagreLastEventId(lastEventId: String): Boolean { return false }
}

class GCPBøtte(private val bøttenavn: String) : Bøtte {
    companion object {
        private const val FILNAVN = "last_event_id.txt"
    }
    override fun hentLastEventId(): String? = String(hentBøtte().get(FILNAVN).getContent())
        .takeIf { it.isNotBlank() }
        ?.also {
            logg.info("Funnet last event id, id=$it")
        }

    override fun lagreLastEventId(lastEventId: String): Boolean {
        logg.info("Lagrer last event id i bøtta, id=$lastEventId")
        return lagre(lastEventId, FILNAVN)
    }

    private fun lagre(tekst: String, filnavn: String): Boolean {
        val bøtte = hentBøtte()
        val blob = bøtte.get(filnavn) ?: null
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
