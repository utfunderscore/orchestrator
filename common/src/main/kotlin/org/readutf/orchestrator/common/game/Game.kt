package org.readutf.orchestrator.common.game

import java.util.UUID

data class Game(
    val id: UUID,
    val serverId: UUID,
    val active: Boolean,
)
