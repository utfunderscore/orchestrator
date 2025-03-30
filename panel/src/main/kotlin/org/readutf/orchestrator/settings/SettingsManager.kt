package org.readutf.orchestrator.settings

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class SettingsManager(val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(SettingsTable)
        }
    }

    private val settings = loadSettings()

    fun updateSettings(editor: Settings.() -> Unit) {
        editor(settings)
        println(settings)
        saveSettings(settings)
    }

    fun getSettings(): Settings = settings

    fun loadSettings(): Settings = transaction(database) {
        SettingsTable.selectAll().firstOrNull()?.let { row ->
            Settings(
                id = row[SettingsTable.id].value,
                roleId = row[SettingsTable.panelRoleId],
                categoryId = row[SettingsTable.categoryId],
                servicesChannelId = row[SettingsTable.servicesChannelId],
            )
        } ?: createSettings()
    }

    fun createSettings(): Settings {
        val id = SettingsTable.insertAndGetId {
        }.value

        return Settings(id, null, null, null)
    }

    fun saveSettings(settings: Settings) = transaction(database) {
        settings.also {
            SettingsTable.upsert {
                it[id] = settings.id
                it[panelRoleId] = settings.roleId
                it[categoryId] = settings.categoryId
                it[servicesChannelId] = settings.servicesChannelId
            }
        }
    }
}
