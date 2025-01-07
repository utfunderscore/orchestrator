package org.readutf.orchestrator.common.utils

data class LongId(
    val longContainerId: String,
) {
    fun toShortId() = ShortId(longContainerId)
}
