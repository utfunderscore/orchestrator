package org.readutf.orchestrator.server.service.platform.docker

import com.github.michaelbull.result.runCatching
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.transactions.transaction
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.TemplateName

class ContainerTemplateTracker(val database: Database) {

    fun storeContainerTemplate(containerId: ShortContainerId, templateName: TemplateName) = runCatching {
        transaction(database) {
            ContainerHistoryTable.replace {
                it[containerIdRow] = containerId.id
                it[templateNameRow] = templateName.value
            }
        }
    }

    fun getTemplateName(containerId: ShortContainerId) = runCatching {
        transaction(database) {
            ContainerHistoryTable.select(ContainerHistoryTable.templateNameRow).where { ContainerHistoryTable.containerIdRow eq containerId.id }.firstOrNull()?.let {
                TemplateName(it[ContainerHistoryTable.templateNameRow])
            } ?: error("Could not find template name")
        }
    }

    private object ContainerHistoryTable : IntIdTable("container_history") {

        val containerIdRow = varchar("container_id", 12)
        val templateNameRow = varchar("template_name", 255)

        init {
            uniqueIndex(containerIdRow)
        }
    }
}
