package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet

data class S2CRecalculateCapacity(
    val numberOfPlayers: Int,
    val creatingGame: Boolean,
    val gameType: String?,
) : Packet()
