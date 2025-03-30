package org.readutf.orchestrator.common.template.docker

import com.fasterxml.jackson.annotation.JsonIgnore
import org.readutf.orchestrator.common.template.ContainerTemplate

data class DockerTemplate(
    val id: String,
    var dockerImage: String,
    var hostName: String? = null,
    val bindings: HashSet<String> = HashSet(),
    val ports: HashSet<String> = HashSet(),
    val environmentVariables: HashSet<String> = HashSet(),
    val commands: HashSet<String> = HashSet(),
    val removeAutomatically: Boolean = true,
) : ContainerTemplate(id) {

    @JsonIgnore
    override fun getShortDescription(): String = "$templateId [type: docker, image: $dockerImage]"

    @JsonIgnore
    override fun getDescription(): String = toString()
}
