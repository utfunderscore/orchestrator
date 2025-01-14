package org.readutf.orchestrator.server.container.impl.docker.store

import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.container.impl.docker.DockerTemplate

interface DockerTemplateStore {
    fun saveTemplate(template: DockerTemplate): SResult<Unit>

    fun getTemplate(templateId: String): SResult<DockerTemplate>

    fun deleteTemplate(templateId: String): SResult<Unit>

    fun getAllTemplates(
        offset: Long,
        limit: Int,
    ): List<String>
}
