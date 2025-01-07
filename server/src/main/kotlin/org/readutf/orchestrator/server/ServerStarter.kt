package org.readutf.orchestrator.server

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

fun main() {
    val properties = Properties()
    properties.load(Orchestrator::class.java.getResourceAsStream("/version.properties"))

    val version = properties.getOrDefault("version", "UNKNOWN")
    val builtAt = properties.getOrDefault("buildTime", "UNKNOWN") as String

    val formattedBuildTime = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(Date(builtAt.toLong()))
    println("   Running Orchestrator Server v$version built on $formattedBuildTime")

    val orchestrator = Orchestrator("0.0.0.0")
}
