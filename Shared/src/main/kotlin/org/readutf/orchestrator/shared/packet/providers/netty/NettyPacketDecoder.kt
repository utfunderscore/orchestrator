package org.readutf.orchestrator.shared.packet.providers.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import org.readutf.orchestrator.shared.packet.Packet
import org.readutf.orchestrator.shared.packet.serializer.PacketSerializer

class NettyPacketDecoder(
    private val packetSerializer: PacketSerializer,
) : ReplayingDecoder<Packet>() {
    override fun decode(
        contex: ChannelHandlerContext?,
        buffer: ByteBuf,
        packets: MutableList<Any>?,
    ) {
        if (buffer.readableBytes() < 2) {
            return
        }

        val length = buffer.readInt()
        if (buffer.readableBytes() < length) {
            return
        }

        val bytes = ByteArray(length)
        buffer.readBytes(bytes)

        val packet = packetSerializer.deserialize(bytes)
    }
}
