package org.readutf.orchestrator.client

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

fun main() {

    println(
        ObjectMapper().writeValueAsString(
            mapOf(
                "players" to listOf(
                    UUID.randomUUID(),
                ),
                "attributes" to mapOf(
                    "elo" to 100
                )
            )
        )
    )

}