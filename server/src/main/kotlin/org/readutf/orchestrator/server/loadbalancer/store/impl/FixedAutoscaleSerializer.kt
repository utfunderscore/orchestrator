package org.readutf.orchestrator.server.loadbalancer.store.impl

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.loadbalancer.Autoscaler
import org.readutf.orchestrator.server.loadbalancer.impl.FixedCountAutoscaler
import org.readutf.orchestrator.server.loadbalancer.store.AutoscaleSerializer

class FixedAutoscaleSerializer(val database: Database) : AutoscaleSerializer {
    private val logger = KotlinLogging.logger { }
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Table)
        }
    }

    override fun save(scalers: Map<TemplateName, Autoscaler>): Result<Unit, Throwable> {
        val validScalers = scalers
            .filter { it.value is FixedCountAutoscaler }
            .map { it.key to it.value as FixedCountAutoscaler }
            .toMap()

        logger.info { "Saving ${validScalers.size} Fixed count autoscalers" }

        return runCatching {
            transaction(database) {
                Table.batchUpsert(validScalers.entries, Table.service, onUpdate = null) { entry ->
                    this[Table.service] = entry.key.value
                    this[Table.scale] = entry.value.count
                }
            }
        }
    }

    override fun create(jsonNode: JsonNode): Result<Autoscaler, Throwable> {
        val scale = jsonNode.get("scale")?.asInt() ?: return Err(Throwable("Missing 'scale' field"))

        return Ok(FixedCountAutoscaler(scale))
    }

    override fun load(): Result<Map<TemplateName, Autoscaler>, Throwable> = runCatching {
        transaction(database) {
            Table.selectAll().associate { TemplateName(it[Table.service]) to FixedCountAutoscaler(it[Table.scale]) }
        }
    }

    object Table : IntIdTable("autoscalers") {
        val service = varchar("service", 50)
        val scale = integer("scale")

        init {
            uniqueIndex(service)
        }
    }
}
