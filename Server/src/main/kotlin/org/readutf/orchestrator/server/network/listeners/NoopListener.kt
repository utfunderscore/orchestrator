package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.Packet

interface NoopListener<INPUT : Packet> : Listener<INPUT, Unit>
