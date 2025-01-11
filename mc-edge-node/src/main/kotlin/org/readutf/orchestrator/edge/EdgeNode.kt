package org.readutf.orchestrator.edge

import org.readutf.orchestrator.edge.finder.TransferAddress
import org.readutf.orchestrator.edge.finder.impl.StaticFinder
import org.readutf.orchestrator.edge.network.ConnectionManager
import org.readutf.orchestrator.edge.packets.ClientAcknowledgeListener
import org.readutf.orchestrator.edge.packets.ServerStatusListener

class EdgeNode {
    private val connectionManager = ConnectionManager("localhost", 25565)

    private val transferFinder = StaticFinder(TransferAddress("localhost", 25566))

    init {
        connectionManager.registerListener(ClientAcknowledgeListener(transferFinder))
        connectionManager.registerListener(ServerStatusListener())
        connectionManager.start()
    }
}

fun main() {
    EdgeNode()
}
