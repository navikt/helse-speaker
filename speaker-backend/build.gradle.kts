import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val micrometerVersion = "1.9.4"
val ktorVersion = "2.1.1"
val logbackVersion: String by project
val postgresqlVersion: String by project
val junitVersion: String by project
val logstashVersion: String by project
val testcontainersPostgresqlVersion: String by project
val flywayVersion: String by project
val hikariVersion: String by project
val kotliqueryVersion: String by project

plugins {
    application
    kotlin("jvm") apply true
    id("io.ktor.plugin") version "2.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
}

application {
    mainClass.set("no.nav.helse.speaker.ApplicationKt")
}

dependencies {
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

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

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
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
}