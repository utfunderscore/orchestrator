package org.readutf.orchestrator.server.service

import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.TemplateName
import java.time.Instant

data class ActiveContainer(
    val containerId: ShortContainerId,
    val templateName: TemplateName,
    val createdAt: Instant,
)
