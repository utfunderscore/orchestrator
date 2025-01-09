package org.readutf.orchestrator.server.container.impl.docker.store.exposed

import org.jetbrains.exposed.sql.Table

object ExposedSchema : Table("templates") {
    val id = varchar("id", 255).primaryKey()
    val dockerImage = varchar("docker_image", 255)
    val hostName = varchar("host_name", 255).nullable()
    val bindings = text("bindings")
}

object ExposedSchemaPorts : Table("template_ports") {
    val id = varchar("id", 255).primaryKey()
    val port = integer("exposedPort")
    val templateId = varchar("template_id", 255)
}
