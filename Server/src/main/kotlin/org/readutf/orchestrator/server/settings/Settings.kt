package org.readutf.orchestrator.server.settings

import com.sksamuel.hoplite.ConfigAlias

data class Settings(
    @ConfigAlias("api-settings") val apiSettings: ApiSettings,
    @ConfigAlias("server-settings") val serversettings: ServerSettings,
)
