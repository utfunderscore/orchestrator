package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

interface Listener<T : Packet, U> : TypedListener<T, HermesChannel, U>
