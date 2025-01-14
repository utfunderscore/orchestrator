package org.readutf.orchestrator.client

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.listeners.annotation.PacketHandler
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.client.client.ClientManager
import org.readutf.orchestrator.client.client.capacity.CapacityHandler
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownHandler
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownListener
import org.readutf.orchestrator.common.packets.C2SRegisterPacket
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConnectionManager(
    val kryoPool: Pool<Kryo>,
    val hostAddress: String,
    val port: Int,
    val containerId: String,
    val capacityHandler: CapacityHandler,
) {
    private val logger = KotlinLogging.logger {}
    private val completionFuture = CompletableFuture<Boolean>()
    private var clientManager: ClientManager? = null
    private var packetManager: PacketManager<*>? = null

    /**
     * Thread will block until the connection is lost
     * @return true if the connection was successful
     */
    fun connectBlocking(connectHandle: (ConnectionManager) -> Unit): Boolean {
        logger.info { "Connecting to server..." }

        val packetManager =
            PacketManager.nettyClient(
                hostName = hostAddress,
                port,
                serializer = KryoPacketSerializer(kryoPool),
            )
        try {
            packetManager.start()
        } catch (e: Exception) {
            return false
        }

        logger.info { "Connected to orchestrator" }

        packetManager.editListeners {
            it.registerAll(this)
        }

        this.packetManager = packetManager

        val serverId = packetManager.sendPacket<UUID>(C2SRegisterPacket(containerId)).join()

        logger.info { "Registered with id $serverId" }

        this.clientManager = ClientManager(serverId, packetManager, capacityHandler)

        println("asdfgfg")

        try {
            connectHandle(this)
        } catch (e: Exception) {
            logger.error(e) { "Error occurred calling connection start handle" }
        }

        return completionFuture.join()
    }

    private fun disconnect() {
        logger.info { "Disconnected from orchestrator" }

        try {
            clientManager?.disconnect()

            logger.info { "Disconnected from server" }
            completionFuture.complete(true)
        } catch (e: Exception) {
            logger.error(e) { }
        }
    }

    @PacketHandler
    fun onDisconnect(channelClosePacket: ChannelClosePacket<*>) {
        disconnect()
    }

    fun registerSafeShutdownListener(safeShutdownHandler: SafeShutdownHandler) {
        packetManager?.editListeners {
            it.registerListener(SafeShutdownListener(safeShutdownHandler))
        }
    }
}
