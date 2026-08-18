package application.poligraf.database.repository

import application.poligraf.database.DatabaseFactory.dbQuery
import application.poligraf.database.tables.UserDeviceTable
import application.poligraf.database.tables.UserTable
import application.poligraf.models.UserDto
import kotlinx.serialization.json.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

interface UserRepository {
    suspend fun getOrCreateUser(firebaseUid: String, email: String, metadata: Map<String, String> = emptyMap()): UUID
    suspend fun syncUser(firebaseUid: String, userDto: UserDto): UUID
}

class UserRepositoryImpl : UserRepository {
    private val emptyJson = Json.parseToJsonElement("{}").jsonObject

    override suspend fun getOrCreateUser(firebaseUid: String, email: String, metadata: Map<String, String>): UUID = dbQuery {
        val existingUser = UserTable.selectAll()
            .where { UserTable.firebaseUid eq firebaseUid }
            .singleOrNull()

        val userId = if (existingUser != null) {
            existingUser[UserTable.id].value
        } else {
            UserTable.insertAndGetId {
                it[UserTable.firebaseUid] = firebaseUid
                it[UserTable.email] = email.ifBlank { null }
                it[UserTable.occupation] = "unspecified"
                it[UserTable.preferences] = emptyJson
                it[UserTable.additionalData] = emptyJson
            }.value
        }
        
        if (metadata.isNotEmpty()) {
            upsertDevice(userId, metadata)
        }
        
        userId
    }

    override suspend fun syncUser(firebaseUid: String, userDto: UserDto): UUID = dbQuery {
        val existingUser = UserTable.selectAll()
            .where { UserTable.firebaseUid eq firebaseUid }
            .singleOrNull()

        if (existingUser != null) {
            val userId = existingUser[UserTable.id].value
            
            // 1. Only update UserTable if we have data to change
            val hasNameChange = userDto.displayName != null && userDto.displayName != existingUser[UserTable.displayName]
            val hasEmailChange = userDto.email != null && userDto.email != existingUser[UserTable.email]

            if (hasNameChange || hasEmailChange) {
                UserTable.update({ UserTable.id eq userId }) {
                    userDto.displayName?.let { name -> it[displayName] = name }
                    // Fallback to empty string if NULL is not allowed by DB yet
                    userDto.email?.let { email -> it[UserTable.email] = email.ifBlank { null } }
                }
            }
            
            if (userDto.metadata.isNotEmpty()) {
                upsertDevice(userId, userDto.metadata)
            }
            userId
        } else {
            // 2. Creating a new user
            val userId = UserTable.insertAndGetId {
                it[UserTable.firebaseUid] = firebaseUid
                // If it's an anonymous user and DB still has NOT NULL, this might fail.
                // We attempt to set null, if it fails, consider dropping the 'users' table 
                // once to let SchemaUtils recreate it with NULLABLE email.
                it[UserTable.email] = userDto.email?.ifBlank { null }
                it[UserTable.displayName] = userDto.displayName
                it[UserTable.preferences] = emptyJson
                it[UserTable.additionalData] = emptyJson
            }.value
            
            if (userDto.metadata.isNotEmpty()) {
                upsertDevice(userId, userDto.metadata)
            }
            userId
        }
    }

    private fun upsertDevice(userId: UUID, metadata: Map<String, String>) {
        val devId = metadata["device_id"] ?: return
        
        val existing = UserDeviceTable.selectAll()
            .where { (UserDeviceTable.userId eq userId) and (UserDeviceTable.deviceId eq devId) }
            .singleOrNull()

        if (existing != null) {
            UserDeviceTable.update({ UserDeviceTable.id eq existing[UserDeviceTable.id] }) {
                it[model] = metadata["device_model"]
                it[osVersion] = metadata["os_version"]
                it[appVersion] = metadata["app_version"]
                it[language] = metadata["language"]
                it[lastSyncAt] = org.jetbrains.exposed.v1.datetime.CurrentDateTime
            }
        } else {
            UserDeviceTable.insert {
                it[this.userId] = userId
                it[this.deviceId] = devId
                it[this.model] = metadata["device_model"]
                it[this.osVersion] = metadata["os_version"]
                it[this.appVersion] = metadata["app_version"]
                it[this.language] = metadata["language"]
            }
        }
    }
}
