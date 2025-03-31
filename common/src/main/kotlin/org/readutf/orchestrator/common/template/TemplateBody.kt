package org.readutf.orchestrator.common.template

data class TemplateBody(
    val image: String,
    val ports: List<Int>,
    val environmentVariables: Map<String, String>,
)
