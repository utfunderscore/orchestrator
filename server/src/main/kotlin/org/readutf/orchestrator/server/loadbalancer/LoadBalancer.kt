package org.readutf.orchestrator.server.loadbalancer

import org.readutf.orchestrator.server.server.RegisteredServer

interface LoadBalancer {
    /**
     * Called to determine the number of needed servers
     * @return The number of servers to create if positive, to delete if negative
     */
    fun loadBalance(servers: Collection<RegisteredServer>): Int

    fun addAwaitingRequest()
}
