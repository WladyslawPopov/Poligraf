import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "UiCore"
            isStatic = true
        }
    }
    
    jvm()
    
    android {
       namespace = "application.liedetector.uicore"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.runtime)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.material.icons)
        }
    }
}
