import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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

            export(projects.app.data)
            export(projects.app.engine)
            export(projects.app.ui)
            export(libs.napier)
            export(libs.multiplatform.settings)

            linkerOpts("-lsqlite3")
        }
    }

    android {
        namespace = "application.poligraf.sharedLogic"
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
            api(projects.app.domain)
            api(projects.app.data)
            api(projects.app.engine)
            api(projects.app.ui)

            implementation(libs.kotlinx.datetime)

            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.material.icons.extended)


            // DI: Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)

            // Network: Ktor
            implementation(libs.ktor.client.core)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Decompose
            implementation(libs.decompose.core)
            implementation(libs.decompose.jetpack)
            implementation(libs.decompose.compose)
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.backhandler)
        }

        androidMain.dependencies {
        }

        nativeMain.dependencies {
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
