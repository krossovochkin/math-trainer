plugins {
    id("org.jetbrains.compose") version "1.1.0"
    id("com.android.application")
    kotlin("android")
}

group = "com.krossovochkin"
version = "1.0"

repositories {
    jcenter()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.4.0")
}

android {
    compileSdkVersion(31)
    defaultConfig {
        applicationId = "com.krossovochkin.android"
        minSdkVersion(24)
        targetSdkVersion(31)
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    signingConfigs {
        create("release") {
            keyAlias = System.getenv()["RELEASE_KEY_ALIAS"]
            keyPassword = System.getenv()["RELEASE_KEY_PASSWORD"]
            storeFile = file("$rootDir/release-keystore-math-trainer.jks")
            storePassword = System.getenv()["RELEASE_STORE_PASSWORD"]
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
}