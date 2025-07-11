val ktorClientVersion = "3.2.1"
val ktorServerVersion = "3.2.1"
val logbackVersion = "1.5.18"
val logstashVersion = "8.1"
val kafkaVersion = "3.9.1"
val gcpBucketVersion = "2.53.3"
val junitVersion = "5.12.1"

val mainClass = "no.nav.helse.speaker.ApplicationKt"

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorClientVersion")
    implementation("io.ktor:ktor-client-cio:$ktorClientVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorClientVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorClientVersion")

    implementation("io.ktor:ktor-server-core:$ktorServerVersion")
    implementation("io.ktor:ktor-server-cio:$ktorServerVersion")
    implementation("io.ktor:ktor-server-auth:$ktorServerVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorServerVersion") {
        exclude(group = "junit")
    }

    implementation("com.google.cloud:google-cloud-storage:$gcpBucketVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.ktor:ktor-client-mock:$ktorClientVersion")
}

tasks {
    kotlin {
        jvmToolchain(21)
    }
    test {
        useJUnitPlatform()
    }

    withType<Jar> {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] =
                configurations.runtimeClasspath.get().joinToString(separator = " ") {
                    it.name
                }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                if (!file.exists()) it.copyTo(file)
            }
        }
    }
}
