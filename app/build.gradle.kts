plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.cataniaunited"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cataniaunited"
        minSdk        = 27
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SERVER_URL", "\"${project.properties["SERVER_URL"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage   = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            all {
                it.useJUnitPlatform()
                it.finalizedBy(tasks.named("jacocoUnitTestReport"))
            }
        }
    }
}

// ——— Jacoco tasks unchanged ———
tasks.register<JacocoReport>("jacocoUnitTestReport") { /* … */ }
tasks.register<JacocoReport>("jacocoAndroidTestReport") { /* … */ }


dependencies {
    // ─── Compose BOM & UI ─────────────────────────────
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.activity:activity-compose:1.8.0")     // needed for enableEdgeToEdge()
    implementation("androidx.compose.ui:ui")                       // core UI
    implementation("androidx.compose.ui:ui-graphics")              // Canvas, draw
    implementation("androidx.compose.ui:ui-tooling-preview")       // @Preview, inspect
    implementation("androidx.compose.material3:material3")         // Material3 components
    implementation("androidx.navigation:navigation-compose:2.8.0")  // NavHost, composable()

    // ─── Kotlin + AndroidX Core ─────────────────────────
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // ─── Networking & Serialization ─────────────────────
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.mockito:mockito-core:5.6.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.6")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    androidTestImplementation("org.mockito:mockito-android:5.6.0")
}
