package org.readutf.orchestrator.common.utils

data class ShortId(
    var shortId: String,
) {
    init {
        if (shortId.length < 12) error("Container id is too short")
        shortId = shortId.substring(0, 12)
    }
}
