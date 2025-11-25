plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.example.redthread"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.redthread"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
}

dependencies {
    // ===== Compose BOM (una sola) =====
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // ===== Core y Activity =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // ===== Compose UI base =====
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ===== Foundation (LazyGrid, animateItemPlacement, etc.) =====
    implementation("androidx.compose.foundation:foundation")

    // ===== Animations (AnimatedContent, Crossfade, etc.) =====
    implementation("androidx.compose.animation:animation")

    // ===== Material 3 =====
    implementation("androidx.compose.material3:material3")

    // ===== Material icons =====
    implementation("androidx.compose.material:material-icons-extended")

    // ===== Navigation Compose =====
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ===== Lifecycle + ViewModel para Compose =====
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ===== Coroutines =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ===== Room (SQLite local) =====
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ===== DataStore (preferencias locales) =====
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ===== Carga de imágenes =====
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ===== SplashScreen =====
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ===== Material Components (para compatibilidad XML) =====
    implementation("com.google.android.material:material:1.12.0")

    // ===== Tests =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ==== AGREGADOS PARA REST ====
    // Retrofit base
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // <-- NUEVO
    // Convertidor JSON con Gson
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // <-- NUEVO
    // OkHttp y logging interceptor
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // <-- NUEVO
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // <-- NUEVO

    //librerias de Test Locales
    testImplementation(libs.junit) //libreria junit
    testImplementation("io.mockk:mockk:1.13.12") //Mock para kotlin
    testImplementation("org.robolectric:robolectric:4.13") //simular pruebas en Android test locales
    //test implementacion UI
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    //librerias para el manejo de reglas de test
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    // GPS (Fused Location Provider)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

}
