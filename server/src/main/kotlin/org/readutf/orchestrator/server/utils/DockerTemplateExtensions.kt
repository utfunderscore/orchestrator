package org.readutf.orchestrator.server.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.PortBinding
import org.readutf.orchestrator.common.template.docker.DockerTemplate

@JsonIgnore
fun DockerTemplate.getDockerBinds(): List<Bind> = bindings.map { Bind.parse(it) }

@JsonIgnore
fun DockerTemplate.getDockerPorts(): List<PortBinding> {
    return ports.map {
        val split = it.split(":")
        when (split.size) {
            1 -> {
                return@map PortBinding.parse(it)
            }
            2 -> {
                return@map PortBinding.parse("${split[0]}:${split[1]}")
            }
            else -> {
                return@map PortBinding.parse("${split[0]}:${split[1]}:${split[2]}")
            }
        }
    }
}
