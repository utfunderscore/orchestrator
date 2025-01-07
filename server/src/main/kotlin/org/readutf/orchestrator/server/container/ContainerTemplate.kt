package org.readutf.orchestrator.server.container

abstract class ContainerTemplate(
    val templateId: String,
) {
    abstract fun getShortDescription(): String

    abstract fun getDescription(): String
}
