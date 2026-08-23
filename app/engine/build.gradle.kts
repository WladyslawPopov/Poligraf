import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    android {
        namespace = "application.poligraf.engine"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Network: Ktor
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Serialization
            api(libs.kotlinx.serialization.json)

            // Settings
            api(libs.multiplatform.settings)

            // DateTime
            api(libs.kotlinx.datetime)

            // SQLDelight
            implementation(libs.sqldelight.coroutines)

            // DI: Koin
            api(libs.koin.core)

            // Logging
            api(libs.napier)

            // Decompose
            implementation(libs.decompose.core)
            implementation(libs.decompose.jetpack)
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.backhandler)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.android)
        }

        nativeMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.darwin)
        }
    }

    sqldelight {
        databases {
            create("PoligrafDatabase").apply {
                packageName.set("application.poligraf.database")
                dialect("app.cash.sqldelight:sqlite-3-38-dialect:${libs.versions.sqliteDriver.get()}")
                schemaOutputDirectory = (file("src/commonMain/sqldelight"))
                migrationOutputDirectory = file("src/commonMain/sqldelight/migrations")
                version = 1
                verifyMigrations = true
            }
        }
    }
}
