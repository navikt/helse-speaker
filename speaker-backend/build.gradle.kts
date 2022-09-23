import java.nio.file.Paths
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainClass = "no.nav.helse.speaker.AppKt"

val postgresqlVersion = "42.5.0"
val junitVersion = "5.9.0"
val logbackClassicVersion = "1.4.0"
val logstashVersion = "7.2"
val rapidsAndRiversVersion = "2022072721371658950659.c1e8f7bf35c6"
val testcontainersPostgresqlVersion = "1.17.3"
val flywayVersion = "9.3.0"
val hikariVersion = "5.0.1"
val kotliqueryVersion = "1.9.0"

plugins {
    kotlin("jvm") version "1.7.10"
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion") {
        exclude("com.fasterxml.jackson.core")
        exclude("com.fasterxml.jackson.dataformat")
    }
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion") {
        exclude("com.fasterxml.jackson.core")
    }
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Jar> {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        from({ Paths.get(project(":speaker-frontend").buildDir.path) }) {
            into("static")
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("$buildDir/libs/${it.name}")
                if (!file.exists())
                    it.copyTo(file)
            }
        }
    }
}