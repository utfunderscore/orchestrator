package org.readutf.orchestrator.shared.packet.serializer.impl

import com.alibaba.fastjson2.JSON
import org.readutf.orchestrator.shared.packet.Packet
import org.readutf.orchestrator.shared.packet.serializer.PacketSerializer

class FastJsonPacketSerializer : PacketSerializer {
    override fun serialize(packet: Packet): ByteArray = JSON.toJSONBytes(packet)

    override fun deserialize(
        bytes: ByteArray,
        targetClass: Class<out Packet>,
    ): Packet = JSON.parseObject(bytes, targetClass) as Packet
}
