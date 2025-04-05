package org.readutf.orchestrator.server.service

import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.platform.ContainerPlatform

class ContainerManager(val containerPlatform: ContainerPlatform) {

    fun getContainersByType(templateName: TemplateName): List<ActiveContainer> = containerPlatform.getContainers().filter {
        it.templateName == templateName
    }
}
