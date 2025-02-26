package org.readutf.orchestrator.client.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.NettyClientPlatform
import org.readutf.orchestrator.client.client.capacity.CapacityHandler
import org.readutf.orchestrator.client.client.game.ActiveGamesProvider
import org.readutf.orchestrator.client.client.game.ActiveGamesTask
import org.readutf.orchestrator.client.client.listeners.SafeShutdownListener
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownHandler
import org.readutf.orchestrator.common.game.Game
import org.readutf.orchestrator.common.game.GameFinderType
import org.readutf.orchestrator.common.game.GameServerSettings
import org.readutf.orchestrator.common.packets.C2SHeartbeatPacket
import org.readutf.orchestrator.common.packets.C2SUpdateAttribute
import org.readutf.orchestrator.common.server.Heartbeat
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientManager(
    private val serverId: UUID,
    private val packetManager: PacketManager<NettyClientPlatform>,
) {
    private val taskExecutor = Executors.newSingleThreadScheduledExecutor()
    private val objectMapper = ObjectMapper()
    var capacityHandler: CapacityHandler = CapacityHandler { 0.0 }
    var safeShutdownHandler: SafeShutdownHandler = SafeShutdownHandler { }
    var shutdownHook: () -> Unit = { }

    init {
        taskExecutor.scheduleAtFixedRate(HeartbeatTask(this), 0, 1, TimeUnit.SECONDS)

        packetManager.editListeners {
            it.registerListener(SafeShutdownListener(this))
        }
    }

    fun capacityHandler(capacityHandler: CapacityHandler) {
        this.capacityHandler = capacityHandler
    }

    fun shutdownHook(shutdownHook: () -> Unit) {
        this.shutdownHook = shutdownHook
    }

    fun safeShutdownHandler(safeShutdownHandler: SafeShutdownHandler) {
        this.safeShutdownHandler = safeShutdownHandler
    }

    fun enableGameSupplier(supportedGames: List<String>, finderTypes: List<GameFinderType>, activeGamesProvider: () -> List<Game>) {
        enableGameSupplier(supportedGames, finderTypes, ActiveGamesProvider(activeGamesProvider))
    }

    fun enableGameSupplier(supportedGames: List<String>, finderTypes: List<GameFinderType>, activeGamesProvider: ActiveGamesProvider) {
        taskExecutor.scheduleAtFixedRate(ActiveGamesTask(this, activeGamesProvider), 0, 1, TimeUnit.SECONDS)
        updateAttribute("gameSettings", GameServerSettings(supportedGames, finderTypes))
    }

    @Synchronized
    fun updateAttribute(key: String, value: Any) {
        packetManager.sendPacket(
            C2SUpdateAttribute(
                serverId,
                key,
                objectMapper.writeValueAsBytes(value),
            ),
        )
    }

    @Synchronized
    fun sendHeartbeat() {
        packetManager.sendPacket(
            C2SHeartbeatPacket(
                serverId,
                Heartbeat(
                    System.currentTimeMillis(),
                    capacityHandler.getCapacity(),
                ),
            ),
        )
    }

    fun disconnect() {
        taskExecutor.shutdown()
        shutdownHook()
    }
}
