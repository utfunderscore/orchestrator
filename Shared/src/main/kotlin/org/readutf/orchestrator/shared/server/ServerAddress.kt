package org.readutf.orchestrator.shared.server

import com.fasterxml.jackson.annotation.JsonProperty

data class ServerAddress(
    @JsonProperty("host") val host: String,
    @JsonProperty("port") val port: Int,
) {
    override fun toString(): String = "$host:$port"
}
