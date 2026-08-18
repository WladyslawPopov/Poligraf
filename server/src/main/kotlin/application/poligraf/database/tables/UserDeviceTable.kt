package application.poligraf.database.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object UserDeviceTable : UUIDTable("user_devices") {
    val userId = reference("user_id", UserTable)
    val deviceId = varchar("device_id", 128) // ANDROID_ID or IDFV
    
    val model = varchar("model", 128).nullable()
    val osVersion = varchar("os_version", 64).nullable()
    val appVersion = varchar("app_version", 32).nullable()
    val language = varchar("language", 10).nullable()
    
    val pushToken = varchar("push_token", 512).nullable()
    
    val lastSyncAt = datetime("last_sync_at").defaultExpression(CurrentDateTime)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init {
        // Each user can have only one record per specific device
        uniqueIndex("unique_user_device", userId, deviceId)
    }
}
