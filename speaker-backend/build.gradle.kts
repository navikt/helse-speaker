import java.nio.file.Paths

val mainClass = "no.nav.helse.speaker.ApplicationKt"

val micrometerVersion = "1.11.4"
val ktorVersion = "2.3.7"
val jacksonVersion = "2.15.2"
val logbackVersion: String by project
val postgresqlVersion: String by project
val junitVersion: String by project
val logstashVersion: String by project
val testcontainersPostgresqlVersion: String by project
val flywayVersion: String by project
val hikariVersion: String by project
val kotliqueryVersion: String by project
val rapidsAndRiversVersion = "2024010209171704183456.6d035b91ffb4"

plugins {
    kotlin("jvm") apply true
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
}

dependencies {
    api("com.nimbusds:nimbus-jose-jwt:9.31")

    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion") {
        exclude(group = "junit")
    }
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("no.nav.security:token-validation-ktor-v2:3.1.0")

    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation(project(":speaker-database"))
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion") {
        exclude("com.fasterxml.jackson.core")
        exclude("com.fasterxml.jackson.dataformat")
    }
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")

    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion") {
        exclude("com.fasterxml.jackson.core")
    }

    testImplementation("no.nav.security:mock-oauth2-server:2.1.1")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    kotlin {
        jvmToolchain(21)
    }
    test {
        useJUnitPlatform()
    }

    withType<Jar> {
        mustRunAfter(":speaker-frontend:npm_run_build")

        archiveBaseName.set("app")

        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        from({ Paths.get(project(":speaker-frontend").layout.buildDirectory.get().toString()) }) {
            into("speaker-frontend/dist")
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                if (!file.exists()) it.copyTo(file)
            }
        }
    }
}
