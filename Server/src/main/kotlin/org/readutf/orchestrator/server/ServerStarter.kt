package org.readutf.orchestrator.server

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import org.readutf.orchestrator.server.settings.Settings
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    println("")
    println("   _____ __                              __   ___")
    println("  / ___// /_  ___  ____  ____ __________/ /  <  /")
    println("  \\__ \\/ __ \\/ _ \\/ __ \\/ __ `/ ___/ __  /   / / ")
    println(" ___/ / / / /  __/ /_/ / /_/ / /  / /_/ /   / /  ")
    println("/____/_/ /_/\\___/ .___/\\__,_/_/   \\__,_/   /_/   ")
    println("               /_/                               ")

    val properties = Properties()
    properties.load(Orchestrator::class.java.getResourceAsStream("/version.properties"))

    val version = properties.getOrDefault("version", "UNKNOWN")
    val builtAt = properties.getOrDefault("buildTime", "UNKNOWN") as String

    val formattedBuildTime = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(Date(builtAt.toLong()))

    println("   Running Shepard Server v$version built on $formattedBuildTime")

    val baseDir = File(System.getProperty("user.dir"))

    val settingsFile = File(baseDir, "settings.yml")
    if (!settingsFile.exists()) {
        Orchestrator::class.java.getResourceAsStream("/settings.yml")?.let { Files.copy(it, settingsFile.toPath()) }
    }

    val settings: Settings =
        ConfigLoaderBuilder
            .default()
            .addFileSource(settingsFile)
            .build()
            .loadConfigOrThrow<Settings>()

    println("   Rest API: ${settings.apiSettings.host}:${settings.apiSettings.port}")
    println("   TCP Socket: ${settings.serverSettings.host}:${settings.serverSettings.port}")

    Orchestrator(settings, baseDir)
}
