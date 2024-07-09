package org.readutf.orchestrator.shared.packet.providers.netty

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelPipeline
import org.readutf.orchestrator.shared.packet.Packet
import org.readutf.orchestrator.shared.packet.PacketManager
import org.readutf.orchestrator.shared.packet.serializer.PacketSerializer

class NettyPacketHandler(
    val packetSerializer: PacketSerializer,
    val packetManager: PacketManager,
    val pipeline: ChannelPipeline,
) : ChannelInboundHandlerAdapter() {
    val logger = KotlinLogging.logger { }

    init {

        pipeline.addLast(
            NettyPacketDecoder(packetSerializer),
            NettyPacketEncoder(packetSerializer),
            this,
        )
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        logger.info { "Netty channel active (${ctx?.channel()?.id()?.asShortText() ?: "Unknown ID"})" }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        logger.info { "Netty channel active (${ctx?.channel()?.id()?.asShortText() ?: "Unknown ID"})" }
    }

    override fun channelRead(
        ctx: ChannelHandlerContext?,
        msg: Any?,
    ) {
        if (msg !is Packet) {
            return
        }

        packetManager.onPacketReceived(msg)
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext?,
        cause: Throwable?,
    ) {
        super.exceptionCaught(ctx, cause)
    }
}
