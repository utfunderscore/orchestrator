package org.readutf.orchestrator.edge.status

data class StatusRequestResponse(
    val version: Version,
    val players: Players,
    val description: Description,
    val enforcesSecureChat: Boolean,
    val previewsChat: Boolean,
)

data class Version(
    val name: String,
    val protocol: Int,
)

data class Players(
    val max: Int,
    val online: Int,
)

data class Description(
    val text: String,
)
