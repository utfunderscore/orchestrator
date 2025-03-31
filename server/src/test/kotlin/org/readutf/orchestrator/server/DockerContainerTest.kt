package org.readutf.orchestrator.server

import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.platform.docker.DockerContainerPlatform
import kotlin.test.Test
import kotlin.test.assertTrue

class DockerContainerTest {

    val containerPlatform = DockerContainerPlatform("unix:///var/run/docker.sock")

    @Test
    fun createAndStartContainer() {
        val result = containerPlatform.createContainer(
            ServiceTemplate(
                TemplateName("test"),
                "orchestrator-proxy:latest",
                hashSetOf(25565),
                mutableMapOf(),
            ),
        )

        assertTrue(result.isOk)
    }

    @Test
    fun createAndStartInvalidContainer() {
        val result = containerPlatform.createContainer(
            ServiceTemplate(
                TemplateName("test-invalid"),
                "invalid-image",
                hashSetOf(25565),
                mutableMapOf(),
            ),
        )
        assertTrue(result.isErr)
    }
}
