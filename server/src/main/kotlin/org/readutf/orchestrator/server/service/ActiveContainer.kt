package org.readutf.orchestrator.server.service

import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.ServiceTemplate
import java.time.Instant

data class ActiveContainer(
    val containerId: ShortContainerId,
    val template: ServiceTemplate,
    val createdAt: Instant,
)
