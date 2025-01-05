package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.annotation.JsonIgnore
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.server.container.ContainerTemplate
import java.util.UUID

class RegisteredServer(
    serverId: UUID,
    displayName: String,
    containerId: String,
    var shuttingDown: Boolean = false,
    val template: ContainerTemplate,
    var lastHeartbeat: Heartbeat,
    val channel: HermesChannel,
) : Server(
        serverId = serverId,
        displayName = displayName,
        containerId = containerId,
    ) {
    @JsonIgnore
    fun getCapacity() = lastHeartbeat.capacity

    companion object {
        fun fromServer(
            server: Server,
            channel: HermesChannel,
            template: ContainerTemplate,
        ): RegisteredServer =
            RegisteredServer(
                serverId = server.serverId,
                displayName = server.displayName,
                containerId = server.containerId,
                lastHeartbeat = Heartbeat(System.currentTimeMillis(), 0f),
                channel = channel,
                template = template,
            )
    }
}
