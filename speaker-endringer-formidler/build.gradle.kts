val ktorClientVersion = "3.0.0-beta-1"
val rapidsAndRiversVersion = "2024010209171704183456.6d035b91ffb4"
val mainClass = "no.nav.helse.speaker.ApplicationKt"

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

    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")

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
