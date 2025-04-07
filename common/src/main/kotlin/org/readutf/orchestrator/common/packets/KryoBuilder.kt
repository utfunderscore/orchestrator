package org.readutf.orchestrator.common.packets

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ByteArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultSerializers.UUIDSerializer
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import com.esotericsoftware.kryo.util.Pool
import org.objenesis.strategy.StdInstantiatorStrategy
import org.readutf.hermes.response.ResponseDataPacket
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.Server
import java.util.UUID

object KryoBuilder {
    fun build(): Kryo {
        val kryo = Kryo()

        kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())

        kryo.register(C2SRegisterPacket::class.java)
        kryo.register(C2SHeartbeatPacket::class.java)
        kryo.register(S2CScheduleShutdown::class.java)
        kryo.register(S2CGameReservePacket::class.java)
        kryo.register(S2CGameRequestPacket::class.java)
        kryo.register(C2SUpdateAttribute::class.java)
        kryo.register(C2SRenewPacket::class.java)
        kryo.register(ResponseDataPacket::class.java)
        kryo.register(Server::class.java)
        kryo.register(Heartbeat::class.java)
        kryo.register(UUID::class.java, UUIDSerializer())
        kryo.register(emptyMap<String, String>()::class.java)
        kryo.register(emptyList<String>()::class.java)
        kryo.register(byteArrayOf(0)::class.java, ByteArraySerializer())

        return kryo
    }

    class KryoPool : Pool<Kryo>(true, true) {
        override fun create(): Kryo = build()
    }
}
