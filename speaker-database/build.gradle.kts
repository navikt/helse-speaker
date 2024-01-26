plugins {
    kotlin("jvm") apply true
    kotlin("plugin.serialization") version "1.9.21"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

group = "no.nav.helse"