plugins {
    alias(libs.plugins.sykepenger.deployable)
    alias(libs.plugins.kotlin.serialization)
}

sykepengerDeployable {
    mainClass = "no.nav.helse.speaker.ApplicationKt"
}

dependencies {
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.server.auth.jwt) {
        exclude(group = "junit")
    }

    implementation(libs.google.cloud.storage)

    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)

    implementation(libs.kafka.clients)

    testImplementation(libs.ktor.client.mock)
}
