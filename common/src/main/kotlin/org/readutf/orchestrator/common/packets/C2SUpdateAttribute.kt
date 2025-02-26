package org.readutf.orchestrator.common.packets

import org.readutf.hermes.Packet
import java.util.UUID

class C2SUpdateAttribute(
    val serverId: UUID,
    val key: String,
    val data: ByteArray,
) : Packet()
