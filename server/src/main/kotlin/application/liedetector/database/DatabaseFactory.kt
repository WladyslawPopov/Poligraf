package application.liedetector.database

import application.liedetector.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init() {
        val appEnv = System.getenv("APP_ENV") ?: throw IllegalStateException("APP_ENV missing!")
        val isProd = appEnv.lowercase() == "prod"
        
        val jdbcUrl = if (isProd) {
            System.getenv("DB_URL") ?: throw IllegalStateException("DB_URL missing!")
        } else {
            System.getenv("DB_URL_TEST") ?: throw IllegalStateException("DB_URL_TEST missing!")
        }
        
        logger.info("DATABASE: INITIALIZING IN [{}] MODE", appEnv.uppercase())

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
        
        // Run Flyway Migrations (Disabled for MVP)
        // runFlyway(dataSource)
        
        val database = Database.connect(dataSource)
        
        transaction(database) {
            addLogger(StdOutSqlLogger)
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

    private fun runFlyway(dataSource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
        
        try {
            flyway.migrate()
            logger.info("FLYWAY: Database migration successful.")
        } catch (e: Exception) {
            logger.error("FLYWAY: Migration failed!", e)
            throw e
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }
}
