package org.readutf.orchestrator.server.settings

import com.sksamuel.hoplite.ConfigAlias

data class ApiSettings(
    val host: String,
    val port: Int,
    @ConfigAlias("virtual-threads") val virtualThreads: Boolean,
)
