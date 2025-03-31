package org.readutf.orchestrator.server.service.template.store

import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName

interface TemplateStore {

    fun update(name: TemplateName, image: String, ports: List<Int>, environmentVariables: Map<String, String>)

    fun load(name: TemplateName): Result<ServiceTemplate, Throwable>

    fun delete(name: String)
}
