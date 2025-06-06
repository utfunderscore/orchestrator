package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.TemplateName
import java.util.UUID
import java.util.concurrent.CompletableFuture

class RegisteredServer(
    serverId: UUID,
    displayName: String,
    shortContainerId: ShortContainerId,
    networkSettings: NetworkSettings,
    attributes: MutableMap<String, JsonNode> = mutableMapOf(),
    template: TemplateName,
    @field:JsonIgnore var shuttingDown: Boolean = false,
    @field:JsonIgnore var lastHeartbeat: Heartbeat,
    @field:JsonIgnore val channel: HermesChannel,
) : Server(
    id = serverId,
    displayName = displayName,
    shortContainerId = shortContainerId,
    networkSettings = networkSettings,
    templateName = template,
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
        ): RegisteredServer = RegisteredServer(
            serverId = server.id,
            displayName = server.displayName,
            shortContainerId = server.shortContainerId,
            networkSettings = server.networkSettings,
            lastHeartbeat = Heartbeat(System.currentTimeMillis(), 0.0),
            template = server.templateName,
            channel = channel,
        )
    }
}
