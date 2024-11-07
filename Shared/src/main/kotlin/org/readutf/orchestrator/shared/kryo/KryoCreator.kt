package org.readutf.orchestrator.shared.kryo

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.packets.*
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

object KryoCreator {
    fun build(): Kryo {
        val kryo = Kryo()

        kryo.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())

        kryo.register(UUID::class.java, UUIDSerializer())

        kryo.register(LinkedHashMap::class.java)
        kryo.register(ServerAddress::class.java)
        kryo.register(GameFinderType::class.java)
        kryo.register(ArrayList::class.java)
        kryo.register(ServerHeartbeat::class.java)
        kryo.register(Server::class.java)
        kryo.register(Class.forName("kotlin.collections.EmptyList"))

        kryo.register(ServerRegisterPacket::class.java)
        kryo.register(ServerGamesUpdatePacket::class.java)
        kryo.register(C2SServerHeartbeatPacket::class.java)
        kryo.register(GameRequestPacket::class.java)
        kryo.register(GameReservePacket::class.java)
        kryo.register(C2SServerAttributeUpdate::class.java)
        kryo.register(ServerAttributeRemove::class.java)
        kryo.register(C2SServerUnregisterPacket::class.java)

        kryo.isRegistrationRequired = false

        return kryo
    }
}
