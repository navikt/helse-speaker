package no.nav.helse.speaker

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringSerializer
import java.net.InetAddress
import java.util.*

private val String.env get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variable $this" }

internal object Kafka : Sender(
    instance = InetAddress.getLocalHost().hostName,
    image = "NAIS_APP_IMAGE".env,
) {
    private val properties =
        Properties().apply {
            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "KAFKA_BROKERS".env)
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "KAFKA_TRUSTSTORE_PATH".env)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KAFKA_CREDSTORE_PASSWORD".env)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "KAFKA_KEYSTORE_PATH".env)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "KAFKA_CREDSTORE_PASSWORD".env)
            put(ProducerConfig.CLIENT_ID_CONFIG, instance)
            put(ProducerConfig.ACKS_CONFIG, "1")
            put(ProducerConfig.LINGER_MS_CONFIG, "0")
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
        }
    private val producer = KafkaProducer(properties, StringSerializer(), StringSerializer())

    override fun send(melding: String) {
        producer.send(ProducerRecord("tbd.rapid.v1", melding))
    }
}
