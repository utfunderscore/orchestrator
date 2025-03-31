package org.readutf.orchestrator.server.service

import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.platform.ContainerPlatform

class ContainerManager(val containerPlatform: ContainerPlatform) {

    fun getContainersByType(template: TemplateName): List<ActiveContainer> = containerPlatform.getContainers().filter {
        it.template.name == template
    }
}
