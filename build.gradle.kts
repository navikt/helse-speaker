val postgresqlVersion = "42.7.2"
val junitVersion = "5.10.0"
val logbackVersion = "1.4.11"
val logstashVersion = "7.4"
val testcontainersPostgresqlVersion = "1.19.0"
val flywayVersion = "9.3.0"
val hikariVersion = "5.0.1"
val kotliqueryVersion = "1.9.0"

plugins {
    kotlin("jvm") version "1.9.21" apply false
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
