package org.readutf.orchestrator.server.server

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.Registration
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.shared.server.Server
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.command.CommandActor
import revxrsal.commands.ktx.returnWithMessage

@Command("server")
class ServerCommand(
    private val kryo: Kryo,
    private val serverManager: ServerManager,
    private val gameManager: GameManager,
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

    @Subcommand("info")
    fun info(
        actor: CommandActor,
        serverId: String,
    ) {
        val server =
            serverManager.getServerByShortId(serverId) ?: returnWithMessage("Could not find server with that id.")
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
            "${server.getShortId()} ${JSON.toJSONString(server, JSONWriter.Feature.PrettyFormat)}",
        )
}
