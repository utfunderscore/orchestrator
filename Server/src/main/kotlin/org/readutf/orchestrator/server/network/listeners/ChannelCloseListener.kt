package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.server.ServerManager

class ChannelCloseListener(
    private val serverManager: ServerManager,
) : Listener<ChannelClosePacket<HermesChannel>> {
    override fun handle(
        packet: ChannelClosePacket<HermesChannel>,
        channel: HermesChannel,
    ) {
        serverManager.unregisterChannel(channel.channelId)
    }
}
