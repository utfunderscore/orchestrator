package org.readutf.orchestrator.edge.network

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.network.packet.client.ClientPacket
import org.readutf.orchestrator.edge.network.listener.PacketListener
import java.io.IOException
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

class ServerManager(
    private val hostname: String,
    private val port: Int,
) {
    private val logger = KotlinLogging.logger { }
    private val stop = AtomicBoolean(false)
    private val server = ServerSocketChannel.open(StandardProtocolFamily.INET)
    private val shutdownFuture = CompletableFuture<Unit>()
    private var packetListeners = CopyOnWriteArrayList<Pair<KClass<out ClientPacket>, (ClientPacket, ClientConnection) -> Unit>>()

    fun start() {
        logger.info { "Starting connection manager on $hostname:$port" }
        server.bind(InetSocketAddress(hostname, port))

        Thread.startVirtualThread(this::listenConnections)

        shutdownFuture.join()
    }

    fun stop() {
        shutdownFuture.complete(Unit)
        logger.info { "Shutdown complete." }
    }

    private fun isActive(): Boolean = !stop.get()

    inline fun <reified T : ClientPacket> registerListener(listener: PacketListener<T>) {
        val mappedListener: (ClientPacket, ClientConnection) -> Unit = { packet, clientConnection ->
            listener.onPacket(packet as T, clientConnection)
        }

        registerListener(mappedListener, T::class)
    }

    fun registerListener(
        listener: (ClientPacket, ClientConnection) -> Unit,
        clazz: KClass<out ClientPacket>,
    ) {
        logger.info { "Registering listener for ${clazz.simpleName}" }
        packetListeners.add(Pair(clazz, listener))
    }

    fun listenConnections() {
        while (isActive()) {
            try {
                val client: SocketChannel = server.accept()
                logger.info { "Accepted connection from ${client.remoteAddress}" }
                ClientConnection(client, packetListeners)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}
