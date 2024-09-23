val ktorClientVersion = "3.0.0-beta-1"
val logstashVersion: String by project
val logbackVersion: String by project
val mainClass = "no.nav.helse.speaker.ApplicationKt"
val kafkaVersion = "3.7.0"
val ktorServerVersion = "2.3.11"
val jacksonVersion = "2.17.1"
val gcpBucketVersion = "2.35.0"

plugins {
    kotlin("jvm") apply true
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
}

repositories {
    mavenCentral()
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.google.cloud:google-cloud-storage:$gcpBucketVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
