plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "application.poligraf"
version = "1.0.0"

application {
    mainClass.set("application.poligraf.ApplicationKt")
}

tasks.withType<JavaExec> {
    // Local development environment setup
    // Using rootProject.file ensures the path is absolute and reachable from anywhere
    val gcpKeyFile = rootProject.file("gcp-key.json")
    
    environment("APP_ENV", "dev") //prod or dev
    environment("DB_URL", "jdbc:postgresql://localhost:5432/poligraf")
    environment("DB_URL_TEST", "jdbc:postgresql://localhost:5432/poligraf_test")
    environment("DB_USER", "krampus")
    environment("DB_PASSWORD", "password123")
    environment("GOOGLE_APPLICATION_CREDENTIALS", gcpKeyFile.absolutePath)
    environment("GCP_PROJECT_ID", "poligraf-72fc9")
}

dependencies {
    implementation(projects.core)
    
    // DI: Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    
    // Logging
    implementation(libs.logback)
    
    // Ktor Server
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverResources)
    implementation(libs.ktor.serverRateLimit)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    
    // Database (Exposed + Postgres)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // AI (Google Vertex AI)
    implementation(libs.google.vertexai)
    
    // Firebase Admin
    implementation(libs.firebase.admin)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
