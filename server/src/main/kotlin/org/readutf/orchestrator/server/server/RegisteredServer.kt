package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.template.ContainerTemplate
import org.readutf.orchestrator.common.utils.ShortId
import java.util.UUID
import java.util.concurrent.CompletableFuture

class RegisteredServer(
    serverId: UUID,
    displayName: String,
    containerId: ShortId,
    networkSettings: NetworkSettings,
    attributes: MutableMap<String, JsonNode> = mutableMapOf(),
    @field:JsonIgnore var shuttingDown: Boolean = false,
    @field:JsonIgnore val template: ContainerTemplate,
    @field:JsonIgnore var lastHeartbeat: Heartbeat,
    @field:JsonIgnore val channel: HermesChannel,
) : Server(
    id = serverId,
    displayName = displayName,
    containerId = containerId,
    networkSettings = networkSettings,
    attributes = attributes,
) {
    @JsonIgnore
    fun getCapacity() = lastHeartbeat.capacity

    fun sendPacket(packet: Packet<*>) {
        channel.sendPacket(packet)
    }

    inline fun <reified T> sendPacketFuture(packet: Packet<T>): CompletableFuture<Result<T, Throwable>> = channel.sendPacketFuture<T>(packet)

    companion object {
        fun fromServer(
            server: Server,
            channel: HermesChannel,
            template: ContainerTemplate,
        ): RegisteredServer = RegisteredServer(
            serverId = server.id,
            displayName = server.displayName,
            containerId = server.containerId,
            networkSettings = server.networkSettings,
            lastHeartbeat = Heartbeat(System.currentTimeMillis(), 0.0),
            channel = channel,
            template = template,
        )
    }
}
