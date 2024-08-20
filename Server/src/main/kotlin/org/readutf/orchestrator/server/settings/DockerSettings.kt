package org.readutf.orchestrator.server.settings

data class DockerSettings(
    val uri: String,
    val maxConnections: Int,
    val responseTimeout: Long,
    val connectionTimeout: Long,
)
