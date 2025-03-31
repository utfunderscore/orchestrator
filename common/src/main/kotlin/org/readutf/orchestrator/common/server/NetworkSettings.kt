package org.readutf.orchestrator.common.server

data class NetworkSettings(
    val internalHost: String,
    val exposedPorts: List<Int>,
)
