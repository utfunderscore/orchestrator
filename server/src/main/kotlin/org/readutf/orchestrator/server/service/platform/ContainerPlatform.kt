package org.readutf.orchestrator.server.service.platform

import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.ActiveContainer

interface ContainerPlatform {

    fun createContainer(serviceTemplate: ServiceTemplate): Result<String, Throwable>

    fun validateImage(imageName: String): Boolean

    fun renewContainer(shortContainerId: ShortContainerId): Result<Unit, Throwable>

    fun getContainers(): List<ActiveContainer>

    fun getTemplate(shortContainerId: ShortContainerId): TemplateName?

    fun getNetworkSettings(shortContainerId: ShortContainerId): NetworkSettings?
}
