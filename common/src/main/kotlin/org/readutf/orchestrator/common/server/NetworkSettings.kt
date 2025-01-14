package org.readutf.orchestrator.common.server

data class NetworkSettings(
    val exposedPorts: List<Int>,
    val internalHost: String,
)
