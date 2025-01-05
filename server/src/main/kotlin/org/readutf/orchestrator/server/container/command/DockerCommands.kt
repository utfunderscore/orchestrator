package org.readutf.orchestrator.server.container.command

import com.github.michaelbull.result.get
import org.readutf.orchestrator.server.container.ContainerController
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.cli.ConsoleActor

@Command("container")
class DockerCommands(
    private val containerController: ContainerController<*>,
) {
    @Subcommand("address")
    fun getAddress(
        actor: ConsoleActor,
        containerId: String,
    ) {
        val result = containerController.getAddress(containerId)
        if (result.isOk) {
            actor.reply("Address: ${result.get()!!.hostAddress}")
        } else {
            actor.reply("Failed to get address: ${result.error}")
        }
    }
}
