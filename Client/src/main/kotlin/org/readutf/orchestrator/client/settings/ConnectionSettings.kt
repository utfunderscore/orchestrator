package org.readutf.orchestrator.client.settings

data class ConnectionSettings(
    val remoteAddress: String,
    val remotePort: Int,
    val autoReconnect: Boolean = true,
    val maxReconnectAttempts: Int = -1,
)
