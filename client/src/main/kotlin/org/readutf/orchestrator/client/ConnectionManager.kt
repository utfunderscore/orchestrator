package org.readutf.orchestrator.client

import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.client.client.ClientManager
import org.readutf.orchestrator.client.platform.ContainerPlatform
import org.readutf.orchestrator.common.packets.C2SRegisterPacket
import org.readutf.orchestrator.common.packets.C2SRenewPacket
import org.readutf.orchestrator.common.packets.KryoBuilder
import java.util.UUID
import java.util.concurrent.CompletableFuture

public class ConnectionManager(
    private val orchestratorHost: String,
    private val platform: ContainerPlatform,
) {

    private var lastServerId: UUID? = null
    private var reconnect = true
    private val kryoPool = KryoBuilder.KryoPool()
    private val logger = KotlinLogging.logger {}
    private var completionFuture = CompletableFuture<ConnectionResult>()
    private var clientManager: ClientManager? = null
    private var packetManager: PacketManager<*>? = null
    private var configure: ClientManager.() -> Unit = {}

    public fun connect() {
        runBlocking {
            async {
                while (reconnect) {
                    val disconnectType = connectBlocking()
                    if (disconnectType.disconnectType == DisconnectType.SHUTDOWN_REQUEST) {
                        logger.info { "Shutdown request received, no longer reconnecting..." }
                        break
                    }

                    logger.info { "Reconnecting in 3 seconds..." }
                    delay(3000)
                }
            }
        }
    }

    /** Thread will block until the connection is lost
     * @return true if the connection was successful
     */
    public fun connectBlocking(): ConnectionResult {
        logger.info { "Connecting to server..." }
        this.completionFuture = CompletableFuture()

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
            return ConnectionResult(null, DisconnectType.ERROR)
        }

        logger.info { "Connected to orchestrator" }

        this.packetManager = packetManager

        val previousServerId = lastServerId
        val serverId = if (previousServerId == null) {
            logger.info { "Registering server" }
            packetManager.sendPacket<UUID>(C2SRegisterPacket(platform.getContainerId(), emptyMap())).join().getOrElse {
                return ConnectionResult(null, DisconnectType.ERROR)
            }
        } else {
            logger.info { "Reconnecting to server..." }
            packetManager.sendPacket<Boolean>(C2SRenewPacket(previousServerId, platform.getContainerId(), emptyMap()))
                .join().getOrElse {
                    return ConnectionResult(null, DisconnectType.ERROR)
                }
            previousServerId
        }

        lastServerId = serverId

        packetManager.editListeners {
            it.registerListener<ChannelClosePacket<*>> { packet ->
                logger.info { "Connection lost..." }
                disconnect(DisconnectType.SUCCESSFUL_CONNECTION)
            }
        }

        logger.info { "Registered with id $serverId" }

        this.clientManager = ClientManager(this, serverId, packetManager).also(configure)

        return completionFuture.join()
    }

    public fun configure(context: ClientManager.() -> Unit): ConnectionManager {
        configure = context
        return this
    }

    public fun disconnect(disconnectType: DisconnectType) {
        logger.info { "Disconnected from orchestrator" }

        try {
            packetManager?.stop()
            clientManager?.disconnect()

            logger.info { "Disconnected from server" }
            completionFuture.complete(ConnectionResult(lastServerId, disconnectType))
        } catch (e: Exception) {
            logger.error(e) { }
        }
    }
}
