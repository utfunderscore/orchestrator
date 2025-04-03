package org.readutf.orchestrator.server.service.template.store.impl

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
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

    override fun save(
        templateName: TemplateName,
        image: String,
        ports: List<Int>,
        environmentVariables: Map<String, String>,
    ) {
        transaction(database) {
            val result = TemplateTable.insertReturning {
                it[nameColumn] = templateName.value
                it[imageColumn] = image
            }.map { it[TemplateTable.id] }.first()

            TemplatePortsTable.batchInsert(ports) { port ->
                this[TemplatePortsTable.portColumn] = port
                this[TemplatePortsTable.templateColumn] = result
            }

            EnvironmentVariablesTable.batchInsert(environmentVariables.entries) { (key, value) ->
                this[EnvironmentVariablesTable.nameColumn] = key
                this[EnvironmentVariablesTable.valueColumn] = value
                this[EnvironmentVariablesTable.templateColumn] = result
            }
        }
    }

    override fun load(): Result<List<ServiceTemplate>, Throwable> {
        val templates = mutableMapOf<String, ServiceTemplate>()
        transaction(database) {
            TemplateTable
                .leftJoin(
                    TemplatePortsTable,
                    onColumn = { id },
                    otherColumn = { templateColumn },
                )
                .leftJoin(
                    EnvironmentVariablesTable,
                    onColumn = { TemplateTable.id },
                    otherColumn = { templateColumn },
                )
                .selectAll().forEach { row ->
                    var name = row[TemplateTable.nameColumn]
                    val template =
                        templates.getOrPut(name) { ServiceTemplate(TemplateName(name), row[TemplateTable.imageColumn]) }
                    val port = row.getOrNull(TemplatePortsTable.portColumn)
                    val varName = row.getOrNull(EnvironmentVariablesTable.nameColumn)
                    val varValue = row.getOrNull(EnvironmentVariablesTable.valueColumn)
                    if (port != null) {
                        template.ports.add(port)
                    }
                    if (varName != null && varValue != null) {
                        template.environmentVariables[varName] = varValue
                    }
                }
        }

        return Ok(templates.values.toList())
    }

    override fun exists(name: TemplateName): Boolean = transaction(database) {
        TemplateTable.select(TemplateTable.id).where { TemplateTable.nameColumn eq name.value }.count() > 0
    }

    override fun setImage(name: TemplateName, image: String) {
        transaction(database) {
            TemplateTable.update({ TemplateTable.nameColumn eq name.value }) {
                it[imageColumn] = image
            }
        }
    }

    override fun addPort(name: TemplateName, port: Int) {
        transaction(database) {
            TemplatePortsTable.insertIgnore {
                it[portColumn] = port
                it[templateColumn] = TemplateTable.select(id).where { TemplateTable.nameColumn eq name.value }
            }
        }
    }

    override fun removePort(name: TemplateName, port: Int) {
        transaction(database) {
            val id = TemplateTable.select(TemplateTable.id).where { TemplateTable.nameColumn eq name.value }.first().let { it[TemplateTable.id] }

            TemplatePortsTable.deleteWhere {
                TemplatePortsTable.id eq id
                portColumn eq port
            }
        }
    }

    override fun setEnvironmentVariable(
        name: TemplateName,
        key: String,
        value: String,
    ) {
        transaction(database) {
            val id = TemplateTable.select(TemplateTable.id).where { TemplateTable.nameColumn eq name.value }.first().let { it[TemplateTable.id] }

            EnvironmentVariablesTable.upsert(EnvironmentVariablesTable.id, EnvironmentVariablesTable.nameColumn) {
                it[nameColumn] = key
                it[valueColumn] = value
                it[templateColumn] = id
            }
        }
    }

    override fun removeEnvironmentVariable(
        name: TemplateName,
        key: String,
    ) {
        transaction(database) {
            val id = TemplateTable.select(TemplateTable.id).where { TemplateTable.nameColumn eq name.value }.first().let { it[TemplateTable.id] }

            EnvironmentVariablesTable.deleteWhere {
                EnvironmentVariablesTable.id eq id
                nameColumn eq key
            }
        }
    }

    override fun delete(name: TemplateName) {
        transaction(database) {
            val id = TemplateTable.select(TemplateTable.id).where { TemplateTable.nameColumn eq name.value }.first().let { it[TemplateTable.id] }

            TemplatePortsTable.deleteWhere {
                templateColumn eq id
            }
            EnvironmentVariablesTable.deleteWhere {
                templateColumn eq id
            }
            TemplateTable.deleteWhere {
                TemplateTable.id eq id
            }
        }
    }

    object TemplateTable : IntIdTable("template") {
        val nameColumn = varchar("name", 255)
        val imageColumn = varchar("image", 255)

        init {
            uniqueIndex(nameColumn)
        }
    }

    object TemplatePortsTable : IntIdTable("template_ports") {
        val templateColumn = reference("template", TemplateTable)
        val portColumn = integer("port")

        init {
            uniqueIndex(templateColumn, portColumn)
        }
    }

    object EnvironmentVariablesTable : IntIdTable("template_environment_variables") {

        val templateColumn = reference("template", TemplateTable)
        val nameColumn = varchar("name", 255)
        val valueColumn = varchar("value", 255)

        init {
            uniqueIndex(templateColumn, nameColumn)
        }
    }
}
