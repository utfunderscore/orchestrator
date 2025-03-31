package org.readutf.orchestrator.common.template

// For JVM backends
@JvmInline
value class TemplateName(val name: String)

data class ServiceTemplate(
    val name: TemplateName,
    var image: String,
    val ports: HashSet<Int> = HashSet(),
    val environmentVariables: Map<String, String> = hashMapOf<String, String>(),
)
