package org.readutf.orchestrator.server.service.template.store.impl

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsertReturning
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.template.store.TemplateStore

class SqlTemplateStore(val database: Database) : TemplateStore {

    private val logger = KotlinLogging.logger { }

    init {
        transaction(database) {
            logger.info { "Initializing template tables" }
            SchemaUtils.create(TemplateTable, TemplatePortsTable, EnvironmentVariablesTable)
        }
    }

    override fun update(
        templateName: TemplateName,
        image: String,
        ports: List<Int>,
        environmentVariables: Map<String, String>,
    ) {
        transaction(database) {
            val result = TemplateTable.upsertReturning(TemplateTable.name, returning = listOf(TemplateTable.id)) {
                it[TemplateTable.name] = templateName.name
                it[TemplateTable.image] = image
            }.map { it[TemplateTable.id] }.first()

            TemplatePortsTable.selectAll().where { TemplatePortsTable.template eq 20 }

            TemplatePortsTable.deleteWhere {
                template eq result
            }
            EnvironmentVariablesTable.deleteWhere {
                template eq result
            }

            TemplatePortsTable.batchInsert(ports) { port ->
                this[TemplatePortsTable.port] = port
                this[TemplatePortsTable.template] = result
            }

            EnvironmentVariablesTable.batchInsert(environmentVariables.entries) { (key, value) ->
                this[EnvironmentVariablesTable.name] = key
                this[EnvironmentVariablesTable.value] = value
                this[EnvironmentVariablesTable.template] = result
            }
        }
    }

    override fun load(name: TemplateName): Result<ServiceTemplate, Throwable> {
        var image: String? = null
        var ports = hashSetOf<Int>()
        var environmentVariables = mutableMapOf<String, String>()
        transaction(database) {
            TemplateTable
                .leftJoin(TemplatePortsTable, onColumn = { TemplateTable.id }, otherColumn = { TemplatePortsTable.template })
                .leftJoin(EnvironmentVariablesTable, onColumn = { TemplateTable.id }, otherColumn = { EnvironmentVariablesTable.template })
                .selectAll().where { TemplateTable.name eq name.name }.forEach { row ->
                    image = row[TemplateTable.image]
                    row.getOrNull(TemplatePortsTable.port)?.let { port -> ports.add(port) }
                    row.getOrNull(EnvironmentVariablesTable.name)?.let { env ->
                        environmentVariables[env] = row[EnvironmentVariablesTable.value]
                    }
                }
        }

        if (image == null) {
            return Err(Throwable("Template not found"))
        }

        return Ok(ServiceTemplate(name, image, ports, environmentVariables))
    }

    override fun exists(name: TemplateName): Boolean = transaction(database) {
        TemplateTable.select(TemplateTable.id).where { TemplateTable.name eq name.name }.count() > 0
    }

    override fun delete(name: String) {
        TODO("Not yet implemented")
    }

    object TemplateTable : IntIdTable("template") {
        val name = varchar("name", 255)
        val image = varchar("image", 255)

        init {
            uniqueIndex(name)
        }
    }

    object TemplatePortsTable : IntIdTable("template_ports") {
        val template = reference("template", TemplateTable)
        val port = integer("port")

        init {
            uniqueIndex(template, port)
        }
    }

    object EnvironmentVariablesTable : IntIdTable("template_environment_variables") {

        val template = reference("template", TemplateTable)
        val name = varchar("name", 255)
        val value = varchar("value", 255)

        init {
            uniqueIndex(template, name)
        }
    }
}
