package org.readutf.orchestrator.server.server

import org.readutf.orchestrator.server.game.GameManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.command.CommandActor

@Command("server")
class ServerCommand(
    private val serverManager: ServerManager,
    private val gameManager: GameManager,
) {
    private val maxDisplayGames = 5

    @Subcommand("list")
    @Command("servers")
    fun list(actor: CommandActor) {
        val allServers = serverManager.getAllServers()
        if (allServers.isEmpty()) {
            actor.reply("There are not servers registered at this time.")
            return
        }

        allServers.forEach { server ->
            actor.reply("${server.getShortId()} (uptime: ${server.getUptimeString()}, address: ${server.address})")
            val games = gameManager.getGamesByServer(server.serverId)
            for ((index, game) in games.withIndex()) {
                if (index >= maxDisplayGames) break
                actor.reply("  - ${game.shortId()} (type: ${game.matchType}, ingame: ${game.teams.flatten().size})")
            }
            if (games.size > maxDisplayGames) {
                actor.reply("    ... and ${games.size - maxDisplayGames} more games.")
            }
        }
    }
}
