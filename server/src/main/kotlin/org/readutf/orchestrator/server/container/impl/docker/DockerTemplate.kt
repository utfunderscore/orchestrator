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
    fun getDockerPorts(): List<PortBinding> {
        return ports.map {
            val split = it.split(":")
            when (split.size) {
                1 -> {
                    return@map PortBinding.parse(it)
                }
                2 -> {
//                    println(split)
//                    if (split[0] == "0") {
//                        println("Using randomized port")
//                        val random = (10000..65535).random()
//                        return@map PortBinding.parse("$random:${split[1]}")
//                    }
                    return@map PortBinding.parse("${split[0]}:${split[1]}")
                }
                else -> {
//                    if (split[1] == "0") {
//                        val random = (10000..65535).random()
//                        return@map PortBinding.parse("${split[0]}:$random:${split[1]}")
//                    }
                    return@map PortBinding.parse("${split[0]}:${split[1]}:${split[2]}")
                }
            }
        }
    }

    @JsonIgnore
    override fun getShortDescription(): String = "$templateId [type: docker, image: $dockerImage]"

    @JsonIgnore
    override fun getDescription(): String = toString()
}
