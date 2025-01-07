package org.readutf.orchestrator.common.server

data class Heartbeat(
    var lastHeartbeat: Long,
    var capacity: Double,
)
