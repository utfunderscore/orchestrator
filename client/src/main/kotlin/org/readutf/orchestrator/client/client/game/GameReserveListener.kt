package org.readutf.orchestrator.client.client.game

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CGameReservePacket

internal class GameReserveListener : TypedListener<S2CGameReservePacket, HermesChannel, Boolean> {
    override fun handle(packet: S2CGameReservePacket, channel: HermesChannel): Result<Boolean, Throwable> = Err(Throwable("Not implemented"))
}
