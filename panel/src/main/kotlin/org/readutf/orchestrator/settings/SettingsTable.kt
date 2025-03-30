package org.readutf.orchestrator.settings

import org.jetbrains.exposed.dao.id.IntIdTable

object SettingsTable : IntIdTable("panel_settings") {

    val panelRoleId = long("panel_role_id").nullable()
    var categoryId = long("category_id").nullable()
    var servicesChannelId = long("servicesId").nullable()
}
