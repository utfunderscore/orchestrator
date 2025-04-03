package org.readutf.orchestrator.server

import org.jetbrains.exposed.sql.Database
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.template.TemplateManager
import org.readutf.orchestrator.server.service.template.store.impl.SqlTemplateStore
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TemplateManagerTests {

    lateinit var templateManager: TemplateManager

    val testIdTracker = AtomicInteger(0)

    @BeforeTest
    fun before() {
        val jdbcUrl = "jdbc:sqlite:file:test?mode=memory&cache=shared"
        val keepAliveConnection = DriverManager.getConnection(jdbcUrl)
        val database = Database.connect(jdbcUrl, "org.sqlite.JDBC")

        val store = SqlTemplateStore(database)
        templateManager = TemplateManager(store)
    }

    @Test
    fun `generate valid template`() {
        val testId = testIdTracker.incrementAndGet()

        val createResult = templateManager.save(
            TemplateName("test$testId"),
            "orchestrator-proxy:latest",
            listOf(80),
            hashMapOf("ENV" to "value"),
        )

        assertTrue(createResult.isOk)
    }

    @Test
    fun `test load template`() {
        val testId = testIdTracker.incrementAndGet()

        val createResult = templateManager.save(
            TemplateName("test-$testId"),
            "orchestrator-proxy:latest",
            listOf(80, 90),
            hashMapOf("ENV" to "value", "DEV" to "test"),
        )
        assertTrue(createResult.isOk)
        val loadResult = templateManager.get(TemplateName("test-$testId"))
        assertTrue(loadResult != null)
    }
}
