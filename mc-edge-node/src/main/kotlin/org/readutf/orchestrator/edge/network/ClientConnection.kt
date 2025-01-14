package org.readutf.orchestrator.edge.network

import net.minestom.scratch.network.NetworkContext
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket
import net.minestom.server.network.packet.server.common.PingResponsePacket
import net.minestom.server.network.packet.server.login.LoginSuccessPacket
import net.minestom.server.network.player.GameProfile
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.channels.SocketChannel
import java.util.UUID
import kotlin.reflect.KClass

class ClientConnection(
    private val socketChannel: SocketChannel,
    private val listeners: List<Pair<KClass<out ClientPacket>, (ClientPacket, ClientConnection) -> Unit>>,
) {
    private val logger = LoggerFactory.getLogger(ClientConnection::class.java)
    private var online = true

    val networkContext = NetworkContext.Async()
    var username: String? = null
    var uuid: String? = null

    init {
        Thread.startVirtualThread { networkLoopRead() }
        Thread.startVirtualThread { networkLoopWrite() }
    }

    private fun networkLoopRead() {
        while (online) {
            this.online =
                networkContext.read({ buffer: NetworkBuffer ->
                    try {
                        buffer.readChannel(socketChannel)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }, { packet: ClientPacket -> this.handlePacket(packet) })
        }
    }

    private fun networkLoopWrite() {
        while (online) {
            this.online =
                networkContext.write { buffer: NetworkBuffer ->
                    try {
                        buffer.writeChannel(socketChannel)
                    } catch (e: IOException) {
                        throw java.lang.RuntimeException(e)
                    }
                }
        }
    }

    fun close() {
        socketChannel.close()
    }

    private fun handlePacket(clientPacket: ClientPacket) {
        for (listener in listeners) {
            if (listener.first.isInstance(clientPacket)) {
                listener.second.invoke(clientPacket, this)
            }
        }

        when (clientPacket) {
            is ClientPingRequestPacket -> {
                this.networkContext.write(PingResponsePacket(clientPacket.number()))
            }
            is ClientLoginStartPacket -> {
                username = clientPacket.username
                uuid = UUID.randomUUID().toString()
                this.networkContext.write(LoginSuccessPacket(GameProfile(UUID.fromString(uuid), username!!, listOf())))
            }
//            is ClientLoginAcknowledgedPacket -> {
//                for (registryPacket in ScratchRegistryTools.instance.registryPackets) {
//                    this.networkContext.write(registryPacket)
//                }
//                this.networkContext.write(FinishConfigurationPacket())
//            }
            else -> {
                logger.debug("Ignoring packet: {}", clientPacket)
            }
        }
    }
}
