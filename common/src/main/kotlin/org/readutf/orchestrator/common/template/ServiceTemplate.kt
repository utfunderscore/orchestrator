package org.readutf.orchestrator.common.template

// For JVM backends
@JvmInline
value class TemplateName(val value: String)

data class ServiceTemplate(
    val name: TemplateName,
    var image: String,
    val ports: HashSet<Int> = HashSet(),
    val environmentVariables: HashMap<String, String> = hashMapOf<String, String>(),
)
