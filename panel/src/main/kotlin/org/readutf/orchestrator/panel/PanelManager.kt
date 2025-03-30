package org.readutf.orchestrator.panel

import dev.minn.jda.ktx.util.BackedReference
import dev.minn.jda.ktx.util.ref
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.Category
import org.readutf.orchestrator.settings.SettingsManager

class PanelManager(val settingsManager: SettingsManager, val guildRef: BackedReference<Guild>, val roleRef: BackedReference<Role>) {

    private val logger = KotlinLogging.logger { }

    val guild by guildRef
    val role by roleRef

    private val category = createCategory()
    init {
        ServicesChannel(settingsManager, guildRef, roleRef, category.ref())
    }

    private fun createCategory(): Category {
        val existingCategory = settingsManager.getSettings().categoryId?.let { categoryId -> guild.getCategoryById(categoryId) }
        if (existingCategory != null) {
            logger.info { "Found existing admin panel category $existingCategory" }
            return existingCategory
        }

        logger.warn { "No admin panel found, creating..." }
        val category = guild.createCategory("admin-panel").apply {
            addRolePermissionOverride(guild.publicRole.idLong, emptyList(), mutableListOf(Permission.VIEW_CHANNEL))
            addRolePermissionOverride(role.idLong, mutableListOf(Permission.VIEW_CHANNEL), emptyList())
        }.complete()

        settingsManager.updateSettings {
            categoryId = category.idLong
        }

        return category
    }
}
