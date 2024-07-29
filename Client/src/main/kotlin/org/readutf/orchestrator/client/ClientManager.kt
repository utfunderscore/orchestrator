package org.readutf.orchestrator.client

import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.client.server.ServerManager
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.ServerAddress
import java.net.SocketException
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ClientManager(
    serverId: UUID,
    serverAddress: ServerAddress,
    gameFinderTypes: MutableList<GameFinderType>,
    supportedGameTypes: MutableList<String>,
    games: Map<UUID, Game>,
    onClose: (games: Map<UUID, Game>) -> Unit,
) {
    private val packetManager: PacketManager<*> =
        PacketManager
            .nettyClient("localhost", 2980, KryoPacketSerializer(KryoCreator.build()))
            .exception(SocketException::class.java) {
                println("ERROR: Socket exception")
                shutdown()
                onClose.invoke(gameManager.games)
            }

    private val networkManager = ClientNetworkManager(packetManager, serverId)
    private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val serverManager = ServerManager(serverId, serverAddress, supportedGameTypes, gameFinderTypes, networkManager, executorService)
    val gameManager = GameManager(networkManager, serverId, executorService)

    init {
        try {
            packetManager.start()
        } catch (e: Exception) {
            shutdown()
            onClose.invoke(gameManager.games)
        }
        serverManager.registerServer()
        games.values.forEach { gameManager.registerGame(it) }
    }

    private fun shutdown() {
        executorService.shutdown()
        serverManager.shutdown()
        networkManager.shutdown()
        packetManager.stop()
    }
}
