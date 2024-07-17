package org.readutf.orchestrator.server.server.store.listeners

import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

interface Listener<T : Packet> : TypedListener<T, HermesChannel>
