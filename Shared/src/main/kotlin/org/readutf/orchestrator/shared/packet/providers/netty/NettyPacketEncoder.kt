package org.readutf.orchestrator.shared.packet.providers.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.readutf.orchestrator.shared.packet.Packet
import org.readutf.orchestrator.shared.packet.serializer.PacketSerializer

class NettyPacketEncoder(
    private val packetSerializer: PacketSerializer,
) : MessageToByteEncoder<Packet>() {
    override fun encode(
        context: ChannelHandlerContext,
        packet: Packet,
        byteBuf: ByteBuf,
    ) {
        packetSerializer.serialize(packet).let {
            byteBuf.writeInt(it.size)
            byteBuf.writeBytes(it)
        }
    }
}
