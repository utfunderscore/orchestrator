package org.readutf.orchestrator.role

import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import org.readutf.orchestrator.settings.SettingsManager
import java.awt.Color

class RoleManager(val guild: Guild, val settingsManager: SettingsManager) {

    private val logger = KotlinLogging.logger { }

    suspend fun createAdminRole(): Role {
        val role = settingsManager.getSettings().roleId?.let { roleId -> guild.getRoleById(roleId) }
        if (role != null) {
            logger.info { "Found existing panel access role" }
            return role
        }
        logger.info { "Creating panel access role" }

        val createRole = guild.createRole()
        with(createRole) {
            setName("panel").setColor(Color.PINK)
            setColor(Color.PINK)
        }
        val createdRole = createRole.await()
        settingsManager.updateSettings {
            roleId = createdRole.idLong
        }
        return createdRole
    }
}
