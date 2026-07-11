package application.liedetector.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object UserTable : UUIDTable("users") {
    // Identity & Security
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val phoneNumber = varchar("phone_number", 32).nullable()
    val isBanned = bool("is_banned").default(false)

    // Profile & Persona
    val displayName = varchar("display_name", 255).nullable()
    val avatarUrl = varchar("avatar_url", 512).nullable()
    val occupation = varchar("occupation", 128).default("unspecified")
    val language = varchar("language", 10).default("en")
    val timezone = varchar("timezone", 64).default("UTC")

    // Economic Engine
    val subscriptionTier = varchar("subscription_tier", 32).default("free")
    val subscriptionExpiresAt = datetime("subscription_expires_at").nullable()
    val balanceTokens = long("balance_tokens").default(0L)
    val isTester = bool("is_tester").default(false)

    // Extensibility
    val preferences = jsonb<JsonObject>("preferences", { it.toString() }, { Json.decodeFromString(it) })
    val additionalData = jsonb<JsonObject>("additional_data", { it.toString() }, { Json.decodeFromString(it) })

    // Stats & Activity
    val totalAnalysesCount = long("total_analyses_count").default(0L)
    val reputationScore = integer("reputation_score").default(100)
    val lastActiveAt = datetime("last_active_at").defaultExpression(CurrentDateTime)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
