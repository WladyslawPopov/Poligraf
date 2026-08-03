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
    )
    
    android {
       namespace = "application.liedetector.data"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(project(":app:engine"))
            api(projects.core) // For DTOs in Repository Impl
            implementation(libs.kotlinx.serialization.json)
            
            api(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
