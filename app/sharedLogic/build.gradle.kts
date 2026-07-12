import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
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
            api(projects.navigation)
            
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
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        nativeMain.dependencies {
            implementation(libs.sqldelight.native.driver)
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
