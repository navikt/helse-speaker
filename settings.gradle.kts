rootProject.name = "helse-speaker"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
include("speaker-frontend", "speaker-backend", "speaker-kafka", "speaker-database")