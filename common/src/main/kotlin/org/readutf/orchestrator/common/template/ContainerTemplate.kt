package org.readutf.orchestrator.common.template

abstract class ContainerTemplate(
    val templateId: String,
) {
    abstract fun getShortDescription(): String

    abstract fun getDescription(): String
}
