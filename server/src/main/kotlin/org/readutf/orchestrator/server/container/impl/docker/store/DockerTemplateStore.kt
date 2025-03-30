package org.readutf.orchestrator.server.container.impl.docker.store

import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.template.docker.DockerTemplate

interface DockerTemplateStore {
    fun saveTemplate(template: DockerTemplate): Result<Unit, Throwable>

    fun getTemplate(templateId: String): Result<DockerTemplate, Throwable>

    fun deleteTemplate(templateId: String): Result<Unit, Throwable>

    fun getAllTemplates(
        offset: Long,
        limit: Int,
    ): List<String>
}
