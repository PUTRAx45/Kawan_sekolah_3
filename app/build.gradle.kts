import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val devHostProp = localProperties.getProperty("dev.host")?.trim() ?: ""
val devModeProp = localProperties.getProperty("dev.mode")?.trim() ?: ""

android {
    namespace = "com.example.kawansekolah"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.kawansekolah"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "USE_EMULATOR", "true")
            buildConfigField("String", "DEV_HOST", "\"$devHostProp\"")
            buildConfigField("String", "DEV_MODE", "\"$devModeProp\"")
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
        }

        create("physicalDebug") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            buildConfigField("boolean", "USE_EMULATOR", "false")
            buildConfigField("String", "DEV_HOST", "\"$devHostProp\"")
            buildConfigField("String", "DEV_MODE", "\"$devModeProp\"")
            buildConfigField("String", "BASE_URL", "\"https://GANTI-DENGAN-URL-BACKEND/\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "USE_EMULATOR", "false")
            buildConfigField("String", "DEV_HOST", "\"\"")
            buildConfigField("String", "DEV_MODE", "\"\"")
            buildConfigField("String", "BASE_URL", "\"https://GANTI-DENGAN-URL-BACKEND/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Koog AI Agents
    implementation("ai.koog:koog-agents:1.3.0")
    implementation("ai.koog:koog-agents-additions:1.3.0-beta")

    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
