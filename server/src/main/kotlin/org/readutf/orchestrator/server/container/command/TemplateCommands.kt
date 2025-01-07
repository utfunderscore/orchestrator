package org.readutf.orchestrator.server.container.command

import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.container.scale.ScaleManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.cli.ConsoleActor

@Command("template")
class TemplateCommands(
    private val containerController: ContainerController<*>,
    private val scaleManager: ScaleManager,
) {
    @Subcommand("scale")
    fun scale(
        actor: ConsoleActor,
        templateId: String,
        targetScale: Int,
    ) {
        scaleManager.scaleDeployment(templateId, targetScale)
        actor.reply("Scale for $templateId has been updated to $targetScale")
    }

    @Subcommand("list")
    fun listTemplates(actor: ConsoleActor) {
        actor.reply("Current templates:")

        for (template in containerController.getTemplates()) {
            actor.reply(" * ${template.getShortDescription()}")
        }
    }
}
