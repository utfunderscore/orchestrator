package org.readutf.orchestrator.client.client.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CGameReservePacket
import java.util.UUID

class GameReserveListener : TypedListener<S2CGameReservePacket, HermesChannel, UUID> {
    override fun handle(packet: S2CGameReservePacket, channel: HermesChannel): UUID {
        TODO()
    }
}
