package org.readutf.orchestrator.server.command

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.Registration
import org.readutf.orchestrator.server.Orchestrator
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.scalable.ServerScaleManager
import org.readutf.orchestrator.shared.server.Server
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.command.CommandActor

@Command("server")
class ServerCommand(
    private val kryo: Kryo,
    private val serverManager: ServerManager,
    private val scaleManager: ServerScaleManager,
) {
    private val maxDisplayGames = 5

    @Subcommand("list")
    fun list(actor: CommandActor) {
        val allServers = serverManager.getAllServers()
        if (allServers.isEmpty()) {
            actor.reply("There are not servers registered at this time.")
            return
        }

        allServers.forEach { server ->
            actor.reply(
                server.getInfoString(),
            )
//            val games = gameManager.getGamesByServer(server.serverId)
//            for ((index, game) in games.withIndex()) {
//                if (index >= maxDisplayGames) break
//                actor.reply("  - ${game.shortId()} (type: ${game.matchType}, ingame: ${game.teams.flatten().size})")
//            }
//            if (games.size > maxDisplayGames) {
//                actor.reply("    ... and ${games.size - maxDisplayGames} more games.")
//            }
        }
    }

    @Subcommand("scale")
    fun scaleType(
        actor: CommandActor,
        serverType: String,
        scale: Int,
    ) {
        val start = System.currentTimeMillis()
        scaleManager.setScale(serverType, scale)

        actor.reply("Settings $serverType scale to $scale instances...")
    }

    @Subcommand("info")
    fun info(
        actor: CommandActor,
        serverId: String,
    ) {
        val server =
            serverManager.getServerByShortId(serverId) ?: let {
                actor.error("Server with id $serverId not found.")
                return
            }
        for (serverInfoLine in getServerInfoLines(server)) {
            actor.reply(serverInfoLine)
        }
    }

    @Subcommand("debug")
    fun debug() {
        var previous: Registration? = null
        var i = 0
        while ((i == 0 || previous != null) && i++ < 5000) {
            previous = kryo.getRegistration(i)
            if (previous != null) {
                println("$i: ${previous.type}")
            } else {
                break
            }
        }
    }

    fun getServerInfoLines(server: Server): List<String> =
        listOf(
            "${server.getShortId()} ${Orchestrator.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(server)}",
        )
}
