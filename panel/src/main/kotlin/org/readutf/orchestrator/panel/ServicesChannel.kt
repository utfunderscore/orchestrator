package org.readutf.orchestrator.panel

import dev.minn.jda.ktx.util.BackedReference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.readutf.orchestrator.settings.SettingsManager

class ServicesChannel(val settingsManager: SettingsManager, val guildRef: BackedReference<Guild>, val roleRef: BackedReference<Role>, val categoryRef: BackedReference<Category>) {

    val guild by guildRef
    val role by roleRef
    val category by categoryRef

    private val logger = KotlinLogging.logger { }

    private val channel = createChannel()

    private fun createChannel(): TextChannel {
        val existingChannel = settingsManager.getSettings().servicesChannelId?.let { categoryId -> guild.getTextChannelById(categoryId) }
        if (existingChannel != null) {
            logger.info { "Found existing services channel" }
            return existingChannel
        }

        logger.warn { "No services channel found. creating..." }
        val channel = category.createTextChannel("services").apply {
            addRolePermissionOverride(guild.publicRole.idLong, emptyList(), mutableListOf(Permission.VIEW_CHANNEL))
            addRolePermissionOverride(role.idLong, mutableListOf(Permission.VIEW_CHANNEL), emptyList())
        }.complete()

        settingsManager.updateSettings {
            servicesChannelId = channel.idLong
        }

        return channel
    }
}
