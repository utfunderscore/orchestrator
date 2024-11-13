package org.readutf.orchestrator.server.server.template

data class ServerTemplate(
    val templateId: String,
    var dockerImage: String,
    var hostName: String? = null,
    val bindings: HashSet<String> = HashSet(),
    val ports: HashSet<String> = HashSet(),
    var network: String? = null,
    val environmentVariables: HashSet<String> = HashSet(),
    val commands: HashSet<String> = HashSet(),
)
