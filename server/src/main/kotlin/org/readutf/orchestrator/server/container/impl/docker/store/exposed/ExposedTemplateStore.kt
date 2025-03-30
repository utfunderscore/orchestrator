@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.container.impl.docker.store.exposed

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.readutf.orchestrator.common.template.docker.DockerTemplate
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.DockerTemplateSchema.BindingsSchema
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.DockerTemplateSchema.CommandsSchema
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.DockerTemplateSchema.EnvVarsSchema
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.DockerTemplateSchema.PortsSchema
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.DockerTemplateSchema.TemplateSchema

class ExposedTemplateStore(
    private val database: Database,
) : DockerTemplateStore {
    private val logger = KotlinLogging.logger { }

    init {
        transaction(database) {
            SchemaUtils.create(TemplateSchema)
            SchemaUtils.create(PortsSchema)
            SchemaUtils.create(EnvVarsSchema)
            SchemaUtils.create(CommandsSchema)
            SchemaUtils.create(BindingsSchema)
        }
    }

    override fun saveTemplate(template: DockerTemplate): Result<Unit, Throwable> {
        logger.info { "Saving template: $template" }

        transaction(database) {
            TemplateSchema.upsert {
                it[id] = template.templateId
                it[dockerImage] = template.dockerImage
                it[hostName] = template.hostName
            }

            for (port in template.ports) {
                PortsSchema.upsert {
                    it[id] = template.templateId
                    it[portBind] = port
                }
            }

            for (command in template.commands) {
                CommandsSchema.upsert {
                    it[id] = template.templateId
                    it[CommandsSchema.command] = command
                }
            }

            for (binding in template.bindings) {
                BindingsSchema.upsert {
                    it[id] = template.templateId
                    it[BindingsSchema.binding] = binding
                }
            }
        }

        return Ok(Unit)
    }

    override fun getTemplate(templateId: String): Result<DockerTemplate, Throwable> = transaction(database) {
        val row: ResultRow = TemplateSchema.selectAll().where(TemplateSchema.id eq templateId).first()
        val ports = PortsSchema.selectAll().where { PortsSchema.id eq templateId }.map { it[PortsSchema.portBind] }
        val envVars = EnvVarsSchema.selectAll().where { EnvVarsSchema.id eq templateId }.map { it[EnvVarsSchema.envVars] }
        val commands = CommandsSchema.selectAll().where { CommandsSchema.id eq templateId }.map { it[CommandsSchema.command] }
        val bindings = BindingsSchema.selectAll().where { BindingsSchema.id eq templateId }.map { it[BindingsSchema.binding] }

        Ok(
            DockerTemplate(
                id = templateId,
                dockerImage = row[TemplateSchema.dockerImage],
                hostName = row[TemplateSchema.hostName],
                bindings = bindings.toHashSet(),
                ports = ports.toHashSet(),
                environmentVariables = envVars.toHashSet(),
                commands = commands.toHashSet(),
            ),
        )
    }

    override fun deleteTemplate(templateId: String): Result<Unit, Throwable> {
        transaction(database) {
            TemplateSchema.deleteWhere { id eq templateId }
            PortsSchema.deleteWhere { id eq templateId }
            CommandsSchema.deleteWhere { id eq templateId }
            BindingsSchema.deleteWhere { id eq templateId }
        }

        return Ok(Unit)
    }

    override fun getAllTemplates(
        offset: Long,
        limit: Int,
    ): List<String> = transaction(database) {
        TemplateSchema
            .select(TemplateSchema.id)
            .limit(limit)
            .offset(offset)
            .map { it[TemplateSchema.id] }
    }
}
