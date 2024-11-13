package org.readutf.orchestrator.server.server.template.store

import org.readutf.orchestrator.server.server.template.ServerTemplate
import org.readutf.orchestrator.shared.utils.Result

interface TemplateStore {
    fun saveTemplate(serverTemplate: ServerTemplate): Result<Unit, String>

    fun loadTemplate(templateId: String): Result<ServerTemplate, String>

    fun deleteTemplate(templateId: String): Result<Unit, String>

    fun loadTemplates(): List<ServerTemplate>
}
