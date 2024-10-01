package org.readutf.orchestrator.client.network

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.util.Pool
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.channel.ChannelOpenPacket
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.Listener
import org.readutf.hermes.platform.netty.NettyClientPlatform
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.shared.kryo.KryoCreator
import java.net.SocketException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class NetworkManager(
    private val orchestratorHost: String,
    private val orchestratorPort: Int,
    private val kryo: Kryo,
    val onDisconnect: () -> Unit,
) {
    val packetManager = createPacketManager()

    private fun createPacketManager(): PacketManager<NettyClientPlatform> {
        val startAwait = CompletableFuture<Unit>()
        val packetManager =
            PacketManager
                .nettyClient(
                    hostName = orchestratorHost,
                    port = orchestratorPort,
                    serializer =
                        KryoPacketSerializer(
                            object : Pool<Kryo>(true, false, 16) {
                                override fun create(): Kryo = KryoCreator.build()
                            },
                        ),
                    executorService = Executors.newSingleThreadExecutor(),
                ).editListeners {
                    it.registerListener<ChannelOpenPacket<*>> {
                        println("Connected to orchestrator")
                        startAwait.complete(null)
                    }
                    it.registerListener(
                        ChannelClosePacket::class.java,
                        object : Listener {
                            override fun acceptPacket(
                                hermesChannel: HermesChannel,
                                packet: Packet,
                            ) {
                                onDisconnect()
                            }
                        },
                    )
                }.exception {
                    it.printStackTrace()
                }.exception(SocketException::class.java) {
                    onDisconnect()
                }.start()

        println("awaiting connection")
        startAwait.join()

        return packetManager
    }

    fun shutdown() {
        packetManager.stop()
    }

    fun sendPacket(packet: Packet) {
        try {
            packetManager.sendPacket(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inline fun <reified T> sendPacketWithResponse(packet: Packet): CompletableFuture<T> = packetManager.sendPacket<T>(packet)
}
