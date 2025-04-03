package org.readutf.orchestrator.panel.components

import dev.minn.jda.ktx.messages.Embed
import org.readutf.orchestrator.common.template.ServiceTemplate
import java.time.Instant

object ServiceComponent {

    fun getServiceComponent(serviceTemplate: ServiceTemplate, players: Int, servers: Int, balancer: String) = Embed {
        title = serviceTemplate.name.value
        field("image", serviceTemplate.image)
        if (players > 1) {
            field("players", players.toString())
        } else {
            field("server", servers.toString())
        }
        field("balancer", balancer.toString())
        timestamp = Instant.now()
    }
}
