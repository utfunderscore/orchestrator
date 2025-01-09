package org.readutf.orchestrator.server.container.impl.docker

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.PortBinding
import org.readutf.orchestrator.server.container.ContainerTemplate

data class DockerTemplate(
    val id: String,
    var dockerImage: String,
    var hostName: String? = null,
    val bindings: HashSet<String> = HashSet(),
    val ports: HashSet<String> = HashSet(),
    var network: String? = null,
    val environmentVariables: HashSet<String> = HashSet(),
    val commands: HashSet<String> = HashSet(),
) : ContainerTemplate(id) {
    @JsonIgnore
    fun getDockerBinds(): List<Bind> = bindings.map { Bind.parse(it) }

    @JsonIgnore
    fun getDockerPorts(): List<PortBinding> = ports.map { PortBinding.parse(it) }

    @JsonIgnore
    override fun getShortDescription(): String = "$templateId [type: docker, image: $dockerImage]"

    @JsonIgnore
    override fun getDescription(): String = toString()
}
