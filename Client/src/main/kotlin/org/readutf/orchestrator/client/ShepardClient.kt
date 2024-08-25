@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.game.GameRequestHandler
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ShepardClient(
    private val orchestratorHost: String,
    private val orchestratorPort: Int,
    val serverAddress: ServerAddress,
) {
    private val serverId = UUID.randomUUID()
    private val gameFinderTypes = mutableListOf<GameFinderType>()
    private val supportedGameTypes = mutableListOf<String>()
    private var shuttingDown = false
    private var reconnecting = false
    private var clientManager: ClientManager? = null
    private var restartScheduler = Executors.newSingleThreadScheduledExecutor()
    private val mainExecutor = Executors.newSingleThreadExecutor()
    private var gameRequestHandler: GameRequestHandler? = null

    private val logger = KotlinLogging.logger { }

    fun start(): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        mainExecutor.submit {
            start(emptyMap())
            future.complete(Unit)
        }
        return future
    }

    private fun start(games: Map<UUID, Game>) {
        logger.info { "Connecting to server ($serverAddress)" }

        reconnecting = false
        clientManager =
            ClientManager(
                orchestratorHost,
                orchestratorPort,
                serverId,
                serverAddress,
                gameFinderTypes,
                supportedGameTypes,
                games,
                gameRequestHandler,
            ) {
                onDisconnect(it)
            }
    }

    fun setGameRequestHandler(gameRequestHandler: GameRequestHandler) {
        this.gameRequestHandler = gameRequestHandler
    }

    fun setGameRequestHandler(gameRequestHandler: (String) -> UUID?) {
        this.gameRequestHandler =
            object : GameRequestHandler {
                override fun handleRequest(gameType: String): UUID? = gameRequestHandler.invoke(gameType)
            }
    }

    fun registerFinderTypes(vararg gameType: GameFinderType): ShepardClient {
        registerFinderTypes(gameType.toList())
        return this
    }

    fun registerFinderTypes(gamesTypes: List<GameFinderType>): ShepardClient {
        gameFinderTypes.addAll(gamesTypes)
        return this
    }

    fun registerGameTypes(vararg gameType: String): ShepardClient {
        registerGameTypes(gameType.toList())
        return this
    }

    fun registerGameTypes(gamesTypes: List<String>): ShepardClient {
        supportedGameTypes.addAll(gamesTypes)
        return this
    }

    fun setAttribute(
        key: String,
        any: Any,
    ) {
        if (!isActive()) throw Exception("Not connected.")
        clientManager!!.serverManager.setAttribute(key, any)
    }

    fun removeAttribute(key: String) {
        if (!isActive()) throw Exception("Not connected.")
        clientManager!!.serverManager.removeAttribute(key)
    }

    fun registerGame(
        id: UUID,
        matchType: String,
        teams: List<List<UUID>>,
        gameState: GameState,
    ) {
        if (clientManager == null) throw Exception("Client must be active before a game can be registered")

        clientManager!!.gameManager.registerGame(id, matchType, teams, gameState)
    }

    fun onDisconnect(games: Map<UUID, Game>) {
        if (!shuttingDown && !reconnecting) {
            logger.info { "Reconnecting..." }
            reconnecting = true
            restartScheduler.schedule({
                mainExecutor.submit { start(games) }
            }, 5, TimeUnit.SECONDS)
        }
    }

    fun isActive(): Boolean {
        logger.info { "Client Manager: $clientManager" }
        logger.info { "Reconnecting?: $reconnecting" }
        return clientManager != null && !reconnecting
    }
}
