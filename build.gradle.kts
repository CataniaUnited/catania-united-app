plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "6.0.1.5171"
    jacoco
}

sonar {
    properties {
        property("sonar.projectKey", "CataniaUnited_catania-united-app")
        property("sonar.organization", "cataniaunited")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths",
            "${project.layout.projectDirectory.asFile}/app/build/reports/jacoco/jacocoUnitTestReport/jacocoUnitTestReport.xml," +
                    "${project.layout.projectDirectory.asFile}/app/build/reports/jacoco/jacocoAndroidTestReport/jacocoAndroidTestReport.xml")
    }
}