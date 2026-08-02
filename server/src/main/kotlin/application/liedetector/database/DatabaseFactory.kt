package application.liedetector.database

import application.liedetector.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    fun init() {
        val appEnv = System.getenv("APP_ENV") ?: throw IllegalStateException("APP_ENV missing!")
        val isProd = appEnv.lowercase() == "prod"
        
        val jdbcUrl = if (isProd) {
            System.getenv("DB_URL") ?: throw IllegalStateException("DB_URL missing!")
        } else {
            System.getenv("DB_URL_TEST") ?: throw IllegalStateException("DB_URL_TEST missing!")
        }
        
        println("DATABASE: INITIALIZING IN [${appEnv.uppercase()}] MODE")
        println("DATABASE: TARGET URL -> $jdbcUrl")

        val user = System.getenv("DB_USER") ?: throw IllegalStateException("DB_USER missing!")
        val password = System.getenv("DB_PASSWORD") ?: throw IllegalStateException("DB_PASSWORD missing!")
        val driverClassName = "org.postgresql.Driver"

        val config = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        
        val dataSource = HikariDataSource(config)
        val database = Database.connect(dataSource)
        
        transaction(database) {
            SchemaUtils.create(
                UserTable,
                UserDeviceTable,
                SubjectTable,
                RecordingTable,
                AnalysisTable,
                FeedbackTable,
                TransactionTable,
                SystemPromptTable,
                AppConfigTable
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }
}
