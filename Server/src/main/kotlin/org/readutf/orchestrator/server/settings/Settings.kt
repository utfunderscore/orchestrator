package org.readutf.orchestrator.server.settings

import com.sksamuel.hoplite.ConfigAlias

data class Settings(
    @ConfigAlias("api") val apiSettings: ApiSettings,
    @ConfigAlias("server") val serverSettings: ServerSettings,
    @ConfigAlias("docker") val dockerSettings: DockerSettings,
)
