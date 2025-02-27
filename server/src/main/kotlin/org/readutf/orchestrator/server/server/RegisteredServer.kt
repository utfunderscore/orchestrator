package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.ShortId
import org.readutf.orchestrator.server.container.ContainerTemplate
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
    serverId = serverId,
    displayName = displayName,
    containerId = containerId,
    networkSettings = networkSettings,
    attributes = attributes,
) {
    @JsonIgnore
    fun getCapacity() = lastHeartbeat.capacity

    fun sendPacket(packet: Packet) {
        channel.sendPacket(packet)
    }

    inline fun <reified T> sendPacketFuture(packet: Packet): CompletableFuture<Result<T, Throwable>> = channel.sendPacketFuture<T>(packet)
        .thenApply { result ->
            result.mapError { err -> Throwable(err) }
        }

    companion object {
        fun fromServer(
            server: Server,
            channel: HermesChannel,
            template: ContainerTemplate,
        ): RegisteredServer = RegisteredServer(
            serverId = server.serverId,
            displayName = server.displayName,
            containerId = server.containerId,
            networkSettings = server.networkSettings,
            lastHeartbeat = Heartbeat(System.currentTimeMillis(), 0.0),
            channel = channel,
            template = template,
        )
    }
}
