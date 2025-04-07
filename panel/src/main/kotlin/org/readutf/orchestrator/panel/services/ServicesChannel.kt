package org.readutf.orchestrator.panel.services

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.BackedReference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.panel.components.ServiceComponent
import org.readutf.orchestrator.proxy.OrchestratorApi
import org.readutf.orchestrator.settings.SettingsManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ServicesChannel(
    jda: JDA,
    orchestratorApi: OrchestratorApi,
    val settingsManager: SettingsManager,
    guildRef: BackedReference<Guild>,
    roleRef: BackedReference<Role>,
    categoryRef: BackedReference<Category>,
) {

    val guild by guildRef
    val role by roleRef
    val category by categoryRef

    private val logger = KotlinLogging.logger { }
    private val channel = createChannel()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val embeds = mutableMapOf<TemplateName, Long>()

    init {
        executor.scheduleAtFixedRate(ServicesTask(orchestratorApi, this), 0, 20, TimeUnit.SECONDS)

        for (message in channel.getHistoryFromBeginning(100).complete().retrievedHistory) {
            if (message.author == jda.selfUser) {
                val embed = message.embeds.firstOrNull() ?: continue
                val templateName = embed.title?.let { TemplateName(it) } ?: continue
                embeds[templateName] = message.idLong
            }
        }
    }

    suspend fun updateEmbeds(templates: List<ServiceTemplate>, servers: List<Server>) {
        for (template in templates) {
            val embedId = embeds[template.name]
            val message = embedId?.let { channel.retrieveMessageById(it).await() }

            if (embedId != null) {
                channel.editMessageEmbedsById(embedId, ServiceComponent.getServiceComponent(template, 0, 0, "")).queue()
            } else {
                channel.sendMessageEmbeds(
                    ServiceComponent.getServiceComponent(
                        serviceTemplate = template,
                        players = 0,
                        servers = servers.count { it.templateName == template.name },
                        balancer = "",
                    ),
                ).queue { message ->
                    embeds[template.name] = message.idLong
                }
            }
        }

        val toRemove = embeds.filter { (templateName, mId) -> templates.none { it.name == templateName } }
        toRemove.forEach {
            val id = it.value
            channel.deleteMessageById(id).queue()
            embeds.remove(it.key)
        }
    }

    private fun createChannel(): TextChannel {
        val existingChannel =
            settingsManager.getSettings().servicesChannelId?.let { categoryId -> guild.getTextChannelById(categoryId) }
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
