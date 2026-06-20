plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Load local.properties so sdk.dir and API keys are available at build time.
// This file is git-ignored; never commit secrets to gradle.properties.
val localProps = java.util.Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}

fun localProp(key: String): String =
    System.getenv(key) ?: localProps.getProperty(key) ?: ""

android {
    namespace = "com.cleanguard.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cleanguard.ai"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "GEMINI_API_KEY",     "\"${localProp("GEMINI_API_KEY")}\"")
        buildConfigField("String", "DEEPSEEK_API_KEY",   "\"${localProp("DEEPSEEK_API_KEY")}\"")
        buildConfigField("String", "VIRUSTOTAL_API_KEY", "\"${localProp("VIRUSTOTAL_API_KEY")}\"")
    }

    signingConfigs {
        // Release signing: reads keystore path and credentials from local.properties or env vars.
        // Set these in local.properties (never commit them):
        //   KEYSTORE_PATH=../keystore/release.keystore
        //   KEYSTORE_PASSWORD=your_store_password
        //   KEY_ALIAS=cleanguard
        //   KEY_PASSWORD=your_key_password
        create("release") {
            val ksPath = localProp("KEYSTORE_PATH")
            if (ksPath.isNotEmpty()) {
                storeFile = rootProject.file(ksPath)
                storePassword = localProp("KEYSTORE_PASSWORD")
                keyAlias = localProp("KEY_ALIAS").ifEmpty { "cleanguard" }
                keyPassword = localProp("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (localProp("KEYSTORE_PATH").isNotEmpty()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    kapt(libs.hilt.compiler.work)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.work.runtime.ktx)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt { correctErrorTypes = true }

