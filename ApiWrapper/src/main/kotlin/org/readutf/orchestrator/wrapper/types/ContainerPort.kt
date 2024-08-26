package org.readutf.orchestrator.wrapper.types

import com.fasterxml.jackson.annotation.JsonProperty

data class ContainerPort(
    @JsonProperty("IP") val ip: String,
    @JsonProperty("PrivatePort") val privatePort: Int,
    @JsonProperty("PublicPort") val publicPort: Int,
    @JsonProperty("Type") val type: String,
)
