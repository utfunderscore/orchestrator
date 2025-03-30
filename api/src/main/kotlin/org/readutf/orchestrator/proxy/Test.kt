package org.readutf.orchestrator.proxy

fun main() {
    val api = OrchestratorApi("localhost")

    api.createDockerTemplate("proxy-1", "orchestrator-proxy", listOf(25565))

    println("done")

    println(api.getTemplates())
}
