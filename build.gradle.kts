plugins {
    kotlin("jvm") version "1.9.23"
    id("org.sonarqube") version "6.0.1.5171"
}

group = "org.catutd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

sonar {
    properties {
        property("sonar.projectKey", "CataniaUnited_catania-united-app")
        property("sonar.organization", "cataniaunited")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}