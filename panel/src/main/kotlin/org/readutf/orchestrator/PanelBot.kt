package org.readutf.orchestrator

import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.util.ref
import dev.rollczi.litecommands.jda.LiteJDAFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import org.jetbrains.exposed.sql.Database
import org.readutf.orchestrator.command.TemplateCommand
import org.readutf.orchestrator.panel.PanelManager
import org.readutf.orchestrator.proxy.OrchestratorApi
import org.readutf.orchestrator.role.RoleManager
import org.readutf.orchestrator.settings.SettingsManager

class PanelBot(val token: String, val guildId: Long) {

    val jda = createJda()
    val guild = jda.getGuildById(guildId) ?: run {
        error("Failed to find guild with id $guildId")
    }
    val orchestratorApi = OrchestratorApi("localhost")
    val settingsManager = SettingsManager(createDatabase())
    val role = runBlocking { RoleManager(guild, settingsManager).createAdminRole() }
    val panelManager = PanelManager(orchestratorApi, jda, settingsManager, guild.ref(), role.ref())

    init {
        LiteJDAFactory.builder(jda).commands(
            TemplateCommand(orchestratorApi),
        ).build()
    }

    fun createJda(): JDA = default(token = token, enableCoroutines = true) {
        enableIntents(GatewayIntent.MESSAGE_CONTENT)
    }.also { it.awaitReady() }

    fun createDatabase(): Database = Database.connect(
        "jdbc:postgresql://postgres:5432/orchestrator",
        driver = "org.postgresql.Driver",
        user = "orchestrator",
        password = "orchestrator",
    )
}

fun main() {
    val logger = KotlinLogging.logger { }

    val token: String = System.getenv().getOrElse("BOT_TOKEN") {
        logger.error { "Missing bot token environment variable" }
        return
    }

    val guildId = System.getenv()["BOT_GUILD"]?.toLongOrNull() ?: run {
        logger.error { "Missing guild id environment variable" }
        return
    }

    PanelBot(token, guildId)
}
