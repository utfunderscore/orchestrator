package org.readutf.orchestrator.client

import java.util.UUID

public data class ConnectionResult(val serverId: UUID?, val disconnectType: DisconnectType)
