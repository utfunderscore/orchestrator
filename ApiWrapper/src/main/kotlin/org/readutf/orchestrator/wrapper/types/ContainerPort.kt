package org.readutf.orchestrator.wrapper.types

data class ContainerPort(
    val ip: String,
    val privatePort: Int,
    val publicPort: Int,
    val type: String,
)
