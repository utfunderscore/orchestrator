package org.readutf.orchestrator.server.docker

class ContainerId(
    val longId: String,
) {
    val shortId: String = longId.take(12)

    override fun toString(): String = longId
}
