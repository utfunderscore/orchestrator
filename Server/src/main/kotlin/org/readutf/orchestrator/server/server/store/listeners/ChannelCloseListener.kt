package org.readutf.orchestrator.server.server.store.listeners

import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.server.server.ServerManager

class ChannelCloseListener(
    private val serverManager: ServerManager,
) : TypedListener<ChannelClosePacket<HermesChannel>, HermesChannel> {
    override fun handle(
        packet: ChannelClosePacket<HermesChannel>,
        channel: HermesChannel,
    ) {
        serverManager.unregisterChannel(channel.channelId)
    }
}
