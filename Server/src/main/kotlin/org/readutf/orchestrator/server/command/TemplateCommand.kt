package org.readutf.orchestrator.server.command

import org.readutf.orchestrator.server.server.template.ServerTemplateManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.command.CommandActor
import java.util.concurrent.TimeUnit

class TemplateCommand(
    private val templateManager: ServerTemplateManager,
) {
    @Command("template create <id> <image>")
    fun createTemplate(
        commandActor: CommandActor,
        id: String,
        image: String,
    ) {
        val templateResult = templateManager.registerTemplate(id, image)

        if (templateResult.isFailure) {
            commandActor.error("Failed to register template: ${templateResult.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully registered template.")
        }
    }

    @Command("template list")
    fun listTemplates(commandActor: CommandActor) {
        val templates = templateManager.getTemplates()

        if (templates.isEmpty()) {
            commandActor.reply("No templates found.")
        } else {
            commandActor.reply("Templates:")
            templates.forEach {
                commandActor.reply("  - ${it.templateId} (${it.dockerImage})")
            }
        }
    }

    @Command("template setimage <id>")
    fun setImage(
        commandActor: CommandActor,
        id: String,
        image: String,
    ) {
        val result = templateManager.setImage(id, image)

        if (result.isFailure) {
            commandActor.error("Failed to set image: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully set image.")
        }
    }

    @Command("template addport <id> <port>")
    fun addPort(
        commandActor: CommandActor,
        id: String,
        port: String,
    ) {
        val result = templateManager.addPort(id, port)

        if (result.isFailure) {
            commandActor.error("Failed to add port: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully added port.")
        }
    }

    @Command("template removeport <id> <port>")
    fun removePort(
        commandActor: CommandActor,
        id: String,
        port: String,
    ) {
        val result = templateManager.removePort(id, port)

        if (result.isFailure) {
            commandActor.error("Failed to remove port: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully removed port.")
        }
    }

    @Command("template sethostname <id> <hostname>")
    fun setHostname(
        commandActor: CommandActor,
        id: String,
        hostname: String,
    ) {
        val result = templateManager.setHostname(id, hostname)

        if (result.isFailure) {
            commandActor.error("Failed to set hostname: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully set hostname.")
        }
    }

    @Command("template addcommand <id> <command>")
    fun addCommand(
        commandActor: CommandActor,
        id: String,
        command: String,
    ) {
        val result = templateManager.addCommand(id, command)

        if (result.isFailure) {
            commandActor.error("Failed to add command: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully added command.")
        }
    }

    @Command("template removecommand <id> <command>")
    fun removeCommand(
        commandActor: CommandActor,
        id: String,
        command: String,
    ) {
        val result = templateManager.removeCommand(id, command)

        if (result.isFailure) {
            commandActor.error("Failed to remove command: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully removed command.")
        }
    }

    @Command("template setnetwork <id> <network>")
    fun addNetwork(
        commandActor: CommandActor,
        id: String,
        network: String,
    ) {
        val result = templateManager.setNetwork(id, network)

        if (result.isFailure) {
            commandActor.error("Failed to add network: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully added network.")
        }
    }

    @Command("template start <id>")
    fun startTemplate(
        commandActor: CommandActor,
        id: String,
    ) {
        val template = templateManager.getTemplate(id) ?: return commandActor.error("Template not found.")

        val start = System.currentTimeMillis()

        val result = templateManager.createServer(template)

        commandActor.reply("Starting server...")

        result
            .orTimeout(1, TimeUnit.MINUTES)
            .thenAccept {
                val taken = System.currentTimeMillis() - start

                commandActor.reply("Server started from template $id in ${taken}ms")
            }.exceptionally {
                commandActor.error("Failed to start server (Timed out): ${it.message}")
                null
            }
    }

    @Command("template addenv <id> <key> <value>")
    fun addEnvironmentVariable(
        commandActor: CommandActor,
        id: String,
        key: String,
        value: String,
    ) {
        val result = templateManager.addEnvironmentVariable(id, "$key=$value")

        if (result.isFailure) {
            commandActor.error("Failed to add environment variable: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully added environment variable.")
        }
    }

    @Command("template removeenv <id> <key>")
    fun removeEnvironmentVariable(
        commandActor: CommandActor,
        id: String,
        key: String,
    ) {
        val result = templateManager.removeEnvironmentVariable(id, key)

        if (result.isFailure) {
            commandActor.error("Failed to remove environment variable: ${result.getErrorOrNull()}")
        } else {
            commandActor.reply("Successfully removed environment variable.")
        }
    }

//    @Command("template delete <id>")
//    fun deleteTemplate(
//        commandActor: CommandActor,
//        id: String,
//    ) {
//        val result = templateManager.delete(id)
//
//        if (result.isFailure) {
//            commandActor.error("Failed to delete template: ${result.getErrorOrNull()}")
//        } else {
//            commandActor.reply("Successfully deleted template.")
//        }
//    }
}
