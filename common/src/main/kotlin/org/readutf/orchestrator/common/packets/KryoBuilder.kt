package org.readutf.orchestrator.common.packets

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers.UUIDSerializer
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import org.objenesis.strategy.StdInstantiatorStrategy
import org.readutf.hermes.response.ResponsePacket
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
        kryo.register(ResponsePacket::class.java)
        kryo.register(Server::class.java)
        kryo.register(Heartbeat::class.java)
        kryo.register(UUID::class.java, UUIDSerializer())

        return kryo
    }
}
