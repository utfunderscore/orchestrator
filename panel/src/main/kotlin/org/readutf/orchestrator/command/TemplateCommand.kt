package org.readutf.orchestrator.command

import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.readutf.orchestrator.common.utils.runAsync
import org.readutf.orchestrator.proxy.OrchestratorApi
import java.awt.Color

@Command(name = "service")
class TemplateCommand(val orchestratorApi: OrchestratorApi) {

    private val logger = KotlinLogging.logger { }

    @Execute(name = "list")
    fun list(@Context event: SlashCommandInteractionEvent) = runAsync {
        val templates = orchestratorApi.getTemplates().await().getOrElse {
            event.hook.sendMessageEmbeds(
                Embed {
                    title = "Error"
                    description = it.message
                    color = Color.RED.rgb
                },
            ).setEphemeral(true).queue()
            return@runAsync
        }

        event
            .replyEmbeds(
                Embed {
                    title = "Templates"
                    templates.forEach { template ->
                        field(name = template.name.value, value = template.image, inline = true)
                    }
                },
            )
            .setEphemeral(true)
            .queue()
    }

    @Execute(name = "create")
    fun create(@Context event: SlashCommandInteractionEvent, @Arg name: String, @Arg image: String) = runAsync {
        event.deferReply().queue()

        val result = orchestratorApi.createService(name, image, emptyList(), hashMapOf()).await()
        result.onSuccess {
            event.hook.sendMessageEmbeds(
                Embed {
                    title = "Service created"
                    field(name = "Name", value = it.name.value)
                    field(name = "Image", value = it.image)
                    color = Color.GREEN.rgb
                },
            ).setEphemeral(true).queue()
        }.onFailure {
            event.hook.sendMessageEmbeds(
                Embed {
                    title = "Error"
                    description = it.message
                    color = Color.RED.rgb
                },
            ).setEphemeral(true).queue()
        }
    }

    @Execute(name = "setimage")
    fun setImage(
        @Context event: SlashCommandInteractionEvent,
        @Arg name: String,
        @Arg image: String,
    ) = runAsync {
        event.deferReply().await()

        orchestratorApi.setTemplate(name, image).await().onSuccess {
            event.hook.sendMessageEmbeds(
                Embed {
                    title = "Image set"
                    field(name = "Name", value = name)
                    field(name = "Image", value = image)
                    color = Color.GREEN.rgb
                },
            ).setEphemeral(true).queue()
        }.onFailure {
            logger.error(it) { "Failed to set the image $name!" }
            event.hook.sendMessageEmbeds(
                Embed {
                    title = "Error"
                    description = "Could not update image for $name"
                    color = Color.RED.rgb
                },
            ).setEphemeral(true).queue()
        }
    }
}
