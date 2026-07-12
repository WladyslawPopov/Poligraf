package application.liedetector.database.repository

import application.liedetector.database.DatabaseFactory.dbQuery
import application.liedetector.database.tables.UserTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.util.*

interface UserRepository {
    suspend fun getOrCreateUser(firebaseUid: String, email: String): UUID
}

class UserRepositoryImpl : UserRepository {
    override suspend fun getOrCreateUser(firebaseUid: String, email: String): UUID = dbQuery {
        val existingUser = UserTable.selectAll()
            .where { UserTable.firebaseUid eq firebaseUid }
            .singleOrNull()

        if (existingUser != null) {
            existingUser[UserTable.id].value
        } else {
            UserTable.insertAndGetId {
                it[UserTable.firebaseUid] = firebaseUid
                it[UserTable.email] = email
                it[UserTable.occupation] = "unspecified"
            }.value
        }
    }
}
