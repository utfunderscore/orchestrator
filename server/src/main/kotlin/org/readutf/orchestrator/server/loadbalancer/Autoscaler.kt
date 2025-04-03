package org.readutf.orchestrator.server.loadbalancer

import org.readutf.orchestrator.server.server.RegisteredServer

abstract class Autoscaler(val name: String) {
    /**
     * Called to determine the number of needed servers
     * @return The number of servers to create if positive, to delete if negative
     */
    abstract fun getNeededResources(servers: Collection<RegisteredServer>): Int

    abstract fun addAwaitingRequest()
}
