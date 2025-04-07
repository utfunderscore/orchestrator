package org.readutf.orchestrator.server

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.jetbrains.exposed.sql.Database
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.platform.docker.DockerContainerPlatform
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertTrue

class DockerContainerTest {

    val jdbcUrl = "jdbc:sqlite:file:test?mode=memory&cache=shared"
    val keepAliveConnection = DriverManager.getConnection(jdbcUrl)
    val database = Database.connect(jdbcUrl, "org.sqlite.JDBC")
    val containerPlatform = DockerContainerPlatform(createDockerClient("unix:///var/run/docker.sock"), database)

    @Test
    fun createAndStartContainer() {
        val result = containerPlatform.createContainer(
            ServiceTemplate(
                TemplateName("test"),
                "orchestrator-proxy:latest",
                hashSetOf(25565),
                hashMapOf(),
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
                hashMapOf(),
            ),
        )
        assertTrue(result.isErr)
    }

    private fun createDockerClient(dockerHost: String): DockerClient {
        val config =
            DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build()

        val client =
            DockerClientImpl.getInstance(
                config,
                ZerodepDockerHttpClient
                    .Builder()
                    .dockerHost(config.dockerHost)
                    .build(),
            )

        return client
    }
}
