package org.readutf.orchesetrator.proxy

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class TemporaryDisconnect {
    @Subscribe
    fun onInitialize(event: ServerPreConnectEvent) {
        event.player.disconnect(Component.text("Transfer success!").color(NamedTextColor.GREEN))
    }
}
