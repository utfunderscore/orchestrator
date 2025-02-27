package org.readutf.orchestrator.client.client.game

import java.util.UUID

public fun interface GameRequestHandler {

    public fun startGame(): UUID
}
