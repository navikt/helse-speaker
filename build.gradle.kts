val postgresqlVersion = "42.5.0"
val junitVersion = "5.9.0"
val logbackVersion = "1.4.0"
val logstashVersion = "7.2"
val testcontainersPostgresqlVersion = "1.17.3"
val flywayVersion = "9.3.0"
val hikariVersion = "5.0.1"
val kotliqueryVersion = "1.9.0"

plugins {
    base
    kotlin("jvm") version "1.7.20" apply false
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
}
//tasks.assemble {
//    dependsOn("npm_run_build")
//}
//
//tasks.check {
//    dependsOn("npm_run_test")
//}

project.buildDir = File("dist")