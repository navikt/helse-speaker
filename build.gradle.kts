val postgresqlVersion = "42.7.2"
val junitVersion = "5.10.2"
val logbackVersion = "1.5.8"
val logstashVersion = "7.4"
val testcontainersPostgresqlVersion = "1.19.5"
val flywayVersion = "9.3.0"
val hikariVersion = "5.0.1"
val kotliqueryVersion = "1.9.0"

plugins {
    kotlin("jvm") version "2.0.20" apply false
    id("com.github.node-gradle.node") version "3.0.1"
}

subprojects {
    ext {
        set("postgresqlVersion", postgresqlVersion)
        set("junitVersion", junitVersion)
        set("logbackVersion", logbackVersion)
        set("logstashVersion", logstashVersion)
        set("testcontainersPostgresqlVersion", testcontainersPostgresqlVersion)
        set("flywayVersion", flywayVersion)
        set("hikariVersion", hikariVersion)
        set("kotliqueryVersion", kotliqueryVersion)
    }
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

project.layout.buildDirectory = File("dist")
