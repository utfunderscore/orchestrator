package org.readutf.orchestrator.service

data class Service(
    val serviceName: String,
    val loadBalancerMode: String,
    val playersConnected: Int,
    val numberOfServers: Int,
)
