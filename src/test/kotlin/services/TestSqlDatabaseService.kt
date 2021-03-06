package services

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private object People : Table() {
    val firstName = varchar("firstName", 40)
    val lastName = varchar("lastName", 40)
}

class TestSqlDatabaseService {
    private lateinit var testDb: SqlDatabaseService

    @BeforeEach
    fun setUp() {
        testDb = SqlDatabaseService(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
    }

    @Test
    fun `test execute method commits transaction`() {
        testDb.execute {
            SchemaUtils.create(People)
            People.insert {
                it[firstName] = "Mark"
                it[lastName] = "Stefanovic"
            }
        }
        val person = testDb.execute {
            People.selectAll().firstOrNull()
        }
        assertEquals("Mark", person?.get(People.firstName))
    }
}