@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.notification.NotificationManager
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.server.server.template.ServerTemplateManager
import org.readutf.orchestrator.shared.notification.impl.ServerRegisterNotification
import org.readutf.orchestrator.shared.notification.impl.ServerUnregisterNotification
import org.readutf.orchestrator.shared.packets.S2CRecalculateCapacity
import org.readutf.orchestrator.shared.packets.S2CServerGracefulShutdownPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterResponse
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import org.readutf.orchestrator.shared.utils.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.*

class ServerManager(
    private val serverStore: ServerStore,
    private val serverTemplateManager: ServerTemplateManager,
) {
    private val logger = KotlinLogging.logger { }
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    private val changingScale = mutableListOf<String>()

    init {
        scheduledExecutor.scheduleAtFixedRate(
            { invalidateExpiredServers() },
            0,
            5,
            SECONDS,
        )
    }

    fun createServer(serverType: String): CompletableFuture<Result<Server, String>> {
        val serverTemplate =
            serverTemplateManager.getTemplate(serverType)
                ?: return completedFuture(Result.failure("Server type $serverType does not exist"))

        return serverTemplateManager.createServer(serverTemplate)
    }

    fun scaleServerType(
        serverType: String,
        scale: Int,
    ): CompletableFuture<Result<Unit, String>> {
        val existing = getServersByType(serverType).filter { server -> !server.pendingDeletion }

        if (changingScale.contains(serverType)) {
            logger.info { "Server type $serverType is already being scaled" }
            return completedFuture(Result.failure("Server type $serverType is already being scaled"))
        }

        if (existing.size == scale) {
            logger.info { "Server type $serverType is already at scale $scale" }
            return completedFuture(Result.failure("Server type $serverType is already at scale $scale"))
        }

        if (existing.size > scale) {
            logger.info { "Scaling down server type $serverType to $scale" }

            existing.sortedBy { it.heartbeat.capacity }.reversed().subList(scale, existing.size).forEach { server ->
                shutdownServer(server.serverId)
            }

            return completedFuture(Result.empty())
        } else {
            logger.info { "Scaling up server type $serverType to $scale" }

            val futures = mutableListOf<CompletableFuture<Result<Server, String>>>()

            (existing.size until scale).forEach { _ ->
                futures.add(createServer(serverType))
            }

            changingScale.add(serverType)

            return CompletableFuture
                .allOf(*futures.toTypedArray())
                .orTimeout(30, SECONDS)
                .thenApply {
                    changingScale.remove(serverType)
                    Result.empty<String>()
                }.exceptionally {
                    changingScale.remove(serverType)
                    logger.error(it) { "Failed to scale server type $serverType" }
                    Result.failure("Failed to scale server type $serverType")
                }
        }
    }

    fun registerServer(server: RegisteredServer): Result<Unit, ServerRegisterResponse> {
        if (!serverTemplateManager.serverTypeExists(server.serverType)) {
            logger.info { "Server tried to register with unsupported server type ${server.serverType}" }
            return Result.failure(ServerRegisterResponse.INVALID_PROTOCOL)
        }

        logger.info { "Registering server ${server.serverId}" }

        NotificationManager.notifyAll(ServerRegisterNotification(server))

        serverTemplateManager.getServerFuture(server.serverId)?.let {
            it.complete(Result.success(server))
            serverTemplateManager.removeServerFuture(server.serverId)
        }

        serverStore.saveServer(server)
        return Result.empty()
    }

    fun getAllServers(): List<RegisteredServer> = serverStore.getAllServers()

    fun unregisterServer(serverId: String) {
        logger.info { "Unregistering server $serverId" }

        NotificationManager.notifyAll(ServerUnregisterNotification(serverId))

        serverStore.removeServer(serverId)?.let {
            // If server was registered
            logger.info { "Server ${it.serverId} has been unregistered" }

            if (it.pendingDeletion) serverTemplateManager.handleShutdown(it)

            it.channel.close()
        }
    }

    fun unregisterChannel(channelId: String) {
        logger.info { "Unregistering socket $channelId" }

        serverStore.getServersByChannel(channelId).forEach { unregisterServer(it.serverId) }
    }

    /**
     * Safely shuts down the server and marks it as pending deletion
     */
    fun shutdownServer(serverId: String): Result<Unit, String> {
        serverStore.markServerForDeletion(serverId)

        val server =
            serverStore.getServerById(serverId)
                ?: return Result.failure("Server $serverId does not exist")

        server.channel.sendPacket(S2CServerGracefulShutdownPacket())

        return Result.empty()
    }

    private fun invalidateExpiredServers() {
        serverStore.getTimedOutServers().forEach {
            logger.info { "Server ${it.serverId} has timed out" }
            unregisterServer(it.serverId)
        }
    }

    fun handleHeartbeat(serverHeartbeat: ServerHeartbeat) {
        logger.debug { "Received heartbeat from ${serverHeartbeat.serverId}" }

        serverStore.updateHeartbeat(serverHeartbeat.serverId, serverHeartbeat)
    }

    fun setAttribute(
        serverId: String,
        attributeName: String,
        typedObject: Any,
    ) {
        serverStore.setAttribute(serverId, attributeName, typedObject)
    }

    fun removeAttribute(
        serverId: String,
        attributeName: String,
    ) {
        serverStore.removeAttribute(serverId, attributeName)
    }

    fun getServerByShortId(shortId: String): Server? = serverStore.getServerByShortId(shortId)

    fun getServerById(serverId: String): Server? = serverStore.getServerById(serverId)

    fun getServersByType(serverType: String): List<RegisteredServer> = serverStore.getServersByType(serverType)

    fun getServersByChannel(channelId: String): List<RegisteredServer> = serverStore.getServersByChannel(channelId)

    fun recalculateCapacity(
        server: RegisteredServer,
        numberOfPlayers: Int,
        creatingGame: Boolean,
        gameType: String? = null,
    ) {
        server.channel.sendPacket(
            S2CRecalculateCapacity(
                numberOfPlayers = numberOfPlayers,
                creatingGame = creatingGame,
                gameType = gameType,
            ),
        )
    }
}
