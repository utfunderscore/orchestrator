package org.readutf.orchestrator.shared.kryo

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import java.util.UUID

object KryoCreator {
    fun build(): Kryo {
        val kryo = Kryo()

        kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())

        kryo.isRegistrationRequired = false

//        kryo.register(Collections.singletonList("")::class.java)
//        kryo.register(Server::class.java)
//        kryo.register(ServerAddress::class.java)
        kryo.register(UUID::class.java, UUIDSerializer())
//        kryo.register(ArrayList::class.java)
//
//        // Packets
//        kryo.register(ServerUnregisterPacket::class.java)
        kryo.register(ServerRegisterPacket::class.java)

        return kryo
    }
}
