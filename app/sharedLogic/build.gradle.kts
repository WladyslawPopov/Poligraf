import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
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
            export(projects.app.data)
            export(projects.app.engine)
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
            api(projects.app.data)
            api(projects.app.engine)
            api(projects.uiCore)
            
            // Settings
            api(libs.multiplatform.settings)
            
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
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
        }

        nativeMain.dependencies {
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    
}
