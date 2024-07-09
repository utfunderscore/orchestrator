package org.readutf.orchestrator.shared.packet.serializer.impl

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import org.readutf.orchestrator.shared.packet.Packet
import org.readutf.orchestrator.shared.packet.serializer.PacketSerializer

class FastJsonPacketSerializer : PacketSerializer {
    override fun serialize(packet: Packet): ByteArray =
        JSON.toJSONBytes(
            mapOf(
                "class" to packet::class.java.name,
                "data" to packet,
            ),
        )

    override fun deserialize(bytes: ByteArray): Packet {
        val jsonObject: JSONObject = JSON.parseObject(bytes)

        val className = jsonObject.getString("class")
        val data = jsonObject.getJSONObject("data")

        return JSON.parseObject(data.toString(), Class.forName(className)) as Packet
    }
}
