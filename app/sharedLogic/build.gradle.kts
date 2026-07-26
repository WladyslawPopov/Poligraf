import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.skie)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
            
            // Export dependencies to make them visible in iOS
            export(projects.core)
            export(projects.uiCore)
            export(libs.multiplatform.settings)
            export(libs.napier)
            
            linkerOpts("-lsqlite3")
        }
    }
    
    android {
       namespace = "application.liedetector.sharedLogic"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(projects.uiCore)
            
            // Network: Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            
            // Settings
            api(libs.multiplatform.settings)

            // DateTime
            implementation(libs.kotlinx.datetime)
            
            // SQLDelight
            implementation(libs.sqldelight.coroutines)
            
            // DI: Koin
            api(libs.koin.core)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // UI Stability
            implementation(libs.compose.runtime)

            // Logging
            api(libs.napier)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.ktor.client.android)

            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
        }

        nativeMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    sqldelight {
        databases {
            create("LieDetectorDatabase") {
                packageName.set("application.liedetector.database")
            }
        }
    }
}
