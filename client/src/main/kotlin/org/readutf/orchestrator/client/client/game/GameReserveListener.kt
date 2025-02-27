package org.readutf.orchestrator.client.client.game

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CGameReservePacket

internal class GameReserveListener : TypedListener<S2CGameReservePacket, HermesChannel, Boolean> {
    override fun handle(packet: S2CGameReservePacket, channel: HermesChannel): Boolean = false
}
