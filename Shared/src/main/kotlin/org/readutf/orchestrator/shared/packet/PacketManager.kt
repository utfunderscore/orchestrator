package org.readutf.orchestrator.shared.packet

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelPipeline
import org.readutf.orchestrator.shared.packet.providers.netty.NettyPacketHandler
import org.readutf.orchestrator.shared.packet.serializer.PacketSerializer
import org.readutf.orchestrator.shared.packet.serializer.impl.FastJsonPacketSerializer

fun main() {
}

class PacketManager {
    private val logger = KotlinLogging.logger { }

    fun onPacketReceived(packet: Packet) {
        logger.info { "Received packet: $packet" }
    }

    companion object {
        fun netty(
            packetSerializer: PacketSerializer = FastJsonPacketSerializer(),
            channelPipeline: ChannelPipeline,
        ): PacketManager {
            val packetManager = PacketManager()
            NettyPacketHandler(packetSerializer, packetManager, channelPipeline)

            return packetManager
        }
    }
}
