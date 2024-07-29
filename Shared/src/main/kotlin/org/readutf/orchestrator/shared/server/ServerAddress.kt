package org.readutf.orchestrator.shared.server

data class ServerAddress(
    val host: String,
    val port: Int,
) {
    override fun toString(): String = "$host:$port"
}
