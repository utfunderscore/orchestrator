package org.readutf.orchestrator.client

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.util.Pool
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.client.capacity.ServerCapacityProducer
import org.readutf.orchestrator.client.heartbeat.HeartbeatTask
import org.readutf.orchestrator.client.listeners.CapacityRequestListener
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.packets.C2SUnregisterPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterResponse
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientManager(
    remoteAddress: String,
    remotePort: Int,
    private val serverId: String,
    private val serverType: String,
    private val localAddress: ServerAddress,
    private val capacityProducer: ServerCapacityProducer,
    private val onConnect: ClientManager.() -> Unit,
    private val onDisconnect: ClientManager.() -> Unit,
) {
    private val logger = KotlinLogging.logger {}
    internal val packetManager = createPacketManager(remoteAddress, remotePort)
    private var connectedSuccessfully: Boolean = false
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val heartbeatTask =
        HeartbeatTask(
            serverId = serverId,
            capacityProducer = capacityProducer,
            packetConsumer = { packet -> packetManager.sendPacket(packet) },
        )

    fun start(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        packetManager.editListeners {
            it.registerListener<ChannelClosePacket<*>> {
                println("Channel closed")
                future.complete(connectedSuccessfully)
            }

            it.registerListener(CapacityRequestListener(capacityProducer))
        }

        try {
            packetManager.start()
        } catch (e: Exception) {
            logger.error(e) { "Failed to start packet manager" }
            return CompletableFuture.completedFuture(false)
        }

        sendRegisterPacket(future)
        onConnect()
        scheduleHeartbeat()

        println("future: $future")

        return future
    }

    private fun sendRegisterPacket(future: CompletableFuture<Boolean>) {
        packetManager
            .sendPacket<ServerRegisterResponse>(
                ServerRegisterPacket(
                    serverId = serverId,
                    serverType = serverType,
                    address = localAddress,
                ),
            ).thenAccept {
                if (it == ServerRegisterResponse.SUCCESS) connectedSuccessfully = true
            }.exceptionally {
                logger.error(it) { "Failed to register with orchestrator" }
                disconnect()
                future.complete(false)
                null
            }
    }

    private fun scheduleHeartbeat() {
        executor.scheduleAtFixedRate(heartbeatTask, 0, 5, TimeUnit.SECONDS)
    }

    fun disconnect() {
        packetManager.sendPacket(
            C2SUnregisterPacket(
                serverId = serverId,
            ),
        )

        onDisconnect()

        packetManager.stop()
    }

    internal fun updateCapacity() {
        heartbeatTask.run()
    }

    private fun createPacketManager(
        orchestratorHost: String,
        orchestratorPort: Int,
    ) = PacketManager
        .nettyClient(
            hostName = orchestratorHost,
            port = orchestratorPort,
            serializer =
                KryoPacketSerializer(
                    object : Pool<Kryo>(true, false) {
                        override fun create(): Kryo = KryoCreator.build()
                    },
                ),
            executorService = Executors.newSingleThreadExecutor(),
        )
}
