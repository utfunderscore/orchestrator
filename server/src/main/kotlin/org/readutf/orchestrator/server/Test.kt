package org.readutf.orchestrator.server

import com.fasterxml.jackson.databind.ObjectMapper
import org.readutf.orchestrator.server.container.impl.docker.DockerTemplate

fun main() {
    print(
        ObjectMapper().writeValueAsString(
            DockerTemplate(
                "test",
                "test",
                ports = HashSet(listOf("0:25565")),
            ),
        ),
    )
}
