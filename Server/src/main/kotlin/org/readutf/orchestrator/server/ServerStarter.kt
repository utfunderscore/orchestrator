package org.readutf.orchestrator.server

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import org.readutf.orchestrator.server.settings.Settings
import java.io.File
import java.nio.file.Files
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

    println("   Running Shepard Server v$version")
    println("")

    val settingsFile = File(System.getProperty("user.dir"), "settings.yml")
    if (!settingsFile.exists()) {
        Orchestrator::class.java.getResourceAsStream("/settings.yml")?.let { Files.copy(it, settingsFile.toPath()) }
    }

    val settings: Settings =
        ConfigLoaderBuilder
            .default()
            .addFileSource(settingsFile)
            .build()
            .loadConfigOrThrow<Settings>()

    Orchestrator(settings)
}
