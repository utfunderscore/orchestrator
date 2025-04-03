package org.readutf.orchestrator.server.service.template.store

import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName

interface TemplateStore {

    fun save(name: TemplateName, image: String, ports: List<Int>, environmentVariables: Map<String, String>)

    fun load(): Result<List<ServiceTemplate>, Throwable>

    fun exists(name: TemplateName): Boolean

    fun setImage(name: TemplateName, image: String)

    fun addPort(name: TemplateName, port: Int)

    fun removePort(name: TemplateName, port: Int)

    fun setEnvironmentVariable(name: TemplateName, key: String, value: String)

    fun removeEnvironmentVariable(name: TemplateName, key: String)

    fun delete(name: TemplateName)
}
