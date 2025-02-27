package org.readutf.orchestrator.client

import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.client.client.ClientManager
import org.readutf.orchestrator.client.platform.ContainerPlatform
import org.readutf.orchestrator.common.packets.C2SRegisterPacket
import org.readutf.orchestrator.common.packets.KryoBuilder
import java.util.UUID
import java.util.concurrent.CompletableFuture

public class ConnectionManager(
    private val orchestratorHost: String,
    private val platform: ContainerPlatform,
) {
    private val kryoPool = KryoBuilder.KryoPool()
    private val logger = KotlinLogging.logger {}
    private val completionFuture = CompletableFuture<Boolean>()
    private var clientManager: ClientManager? = null
    private var packetManager: PacketManager<*>? = null
    private var configure: ClientManager.() -> Unit = {}

    /**
     * Thread will block until the connection is lost
     * @return true if the connection was successful
     */
    public fun connectBlocking(): Boolean {
        logger.info { "Connecting to server..." }

        val packetManager =
            PacketManager.nettyClient(
                hostName = orchestratorHost,
                port = 2323,
                serializer = KryoPacketSerializer(kryoPool),
            )
        try {
            packetManager.start()
        } catch (e: Exception) {
            logger.error(e) { "Failed to start packet manager" }
            return false
        }

        logger.info { "Connected to orchestrator" }

        packetManager.editListeners {
            it.registerAll(this)
        }

        this.packetManager = packetManager

        val serverId = packetManager.sendPacket<UUID>(C2SRegisterPacket(platform.getContainerId(), emptyMap())).join().getOrElse {
            return false
        }

        packetManager.editListeners {
            it.registerListener<ChannelClosePacket<*>> { packet ->
                logger.info { "Connection lost..." }
                disconnect()
            }
        }

        logger.info { "Registered with id $serverId" }

        this.clientManager = ClientManager(serverId, packetManager).also(configure)

        return completionFuture.join()
    }

    public fun configure(context: ClientManager.() -> Unit): ConnectionManager {
        configure = context
        return this
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
}
