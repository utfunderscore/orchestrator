package org.readutf.orchestrator.shared.server

data class ServerHeartbeat(
    val serverId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val capacity: Float = 0.0f,
) {
    init {
        // Ensure that capacity is between 0 and 1
        if (capacity < 0 || capacity > 1) {
            throw IllegalArgumentException("Capacity must be between 0 and 1")
        }
    }
}
