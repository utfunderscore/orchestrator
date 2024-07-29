package org.readutf.orchestrator.server.utils

object TimeUtils {
    fun formatDuration(duration: Long): String {
        val hours = Math.floorDiv(duration, 3600000)

        if (hours.toInt() != 0) {
            return if (hours > 1) {
                "$hours hours"
            } else {
                "$hours hour"
            }
        } else {
            val minutes = Math.floorDiv(duration, 60000)
            if (minutes.toInt() != 0) {
                return if (minutes > 1) {
                    "$minutes minutes"
                } else {
                    "$minutes minute"
                }
            } else {
                val seconds = Math.floorDiv(duration, 1000)
                return if (seconds.toInt() != 0) {
                    if (seconds > 1) {
                        "$seconds seconds"
                    } else {
                        "$seconds second"
                    }
                } else {
                    "0 seconds"
                }
            }
        }
    }
}
