package org.readutf.orchestrator.client.client.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CGameRequestPacket
import java.util.UUID

class GameRequestListener : TypedListener<S2CGameRequestPacket, HermesChannel, UUID> {
    override fun handle(packet: S2CGameRequestPacket, channel: HermesChannel): UUID {
        TODO()
    }
}
