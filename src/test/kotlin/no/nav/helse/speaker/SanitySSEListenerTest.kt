package no.nav.helse.speaker

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class SanitySSEListenerTest {
    @Test
    fun `kobler til på nytt når SSE-tilkoblingen avsluttes normalt`() {
        var tilkoblinger = 0

        assertThrows(CancellationException::class.java) {
            runBlocking {
                keepListening(delayBeforeReconnect = Duration.ZERO) {
                    tilkoblinger++
                    if (tilkoblinger == 2) throw CancellationException("Avslutter testen")
                }
            }
        }

        assertEquals(2, tilkoblinger)
    }

    @Test
    fun `kobler til på nytt når SSE-tilkoblingen feiler`() {
        var tilkoblinger = 0

        assertThrows(CancellationException::class.java) {
            runBlocking {
                keepListening(delayBeforeReconnect = Duration.ZERO) {
                    tilkoblinger++
                    if (tilkoblinger == 1) throw IllegalStateException("Tilkoblingen feilet")
                    throw CancellationException("Avslutter testen")
                }
            }
        }

        assertEquals(2, tilkoblinger)
    }

    @Test
    fun `kobler ikke til på nytt etter kansellering`() {
        var tilkoblinger = 0

        assertThrows(CancellationException::class.java) {
            runBlocking {
                keepListening(delayBeforeReconnect = Duration.ZERO) {
                    tilkoblinger++
                    throw CancellationException("Avslutter lytteren")
                }
            }
        }

        assertEquals(1, tilkoblinger)
    }
}
