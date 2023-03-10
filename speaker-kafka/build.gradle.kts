import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainClass = "no.nav.helse.speaker.AppKt"

val flywayVersion: String by project
val logbackVersion: String by project
val postgresqlVersion: String by project
val junitVersion: String by project
val logstashVersion: String by project
val testcontainersPostgresqlVersion: String by project
val hikariVersion: String by project
val kotliqueryVersion: String by project
val cloudSqlProxyVersion = "1.7.1"
val mockkVersion = "1.13.2"
val rapidsAndRiversVersion = "2022100711511665136276.49acbaae4ed4"

plugins {
    kotlin("jvm") apply true
    kotlin("plugin.serialization") version "1.8.10"
}

dependencies {
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion") {
        exclude("com.fasterxml.jackson.core")
        exclude("com.fasterxml.jackson.dataformat")
    }
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    implementation("com.google.cloud.sql:postgres-socket-factory:$cloudSqlProxyVersion") {
        exclude("commons-codec")
    }
    implementation("commons-codec:commons-codec:1.15")

    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion") {
        exclude("com.fasterxml.jackson.core")
    }
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation(project(":speaker-database"))

    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion") {
        exclude("com.fasterxml.jackson.core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Jar> {
        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
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