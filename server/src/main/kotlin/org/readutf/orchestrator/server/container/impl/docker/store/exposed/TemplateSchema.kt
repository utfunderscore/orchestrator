package org.readutf.orchestrator.server.container.impl.docker.store.exposed

import org.jetbrains.exposed.sql.Table
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.DockerTemplateSchema.EnvVarsSchema.uniqueIndex

object DockerTemplateSchema {
    object TemplateSchema : Table("template") {
        val id = varchar("id", 255).uniqueIndex("INX_template_id")
        val dockerImage = varchar("docker_image", 255)
        val hostName = varchar("host_name", 255).nullable()
        val network = varchar("network", 255).nullable()

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id, name = "PK_template_id")
    }

    object PortsSchema : Table("template_ports") {
        val id = optReference("id", TemplateSchema.id).uniqueIndex()
        val portBind = varchar("portBind", 16)

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id, name = "PK_template_ports_id")
    }

    object EnvVarsSchema : Table("template_environment_variables") {
        val id = optReference("id", TemplateSchema.id).uniqueIndex()
        val envVars = varchar("environment_variable", 255)

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id, name = "PK_template_env_vars_id")
    }

    object CommandsSchema : Table("template_commands") {
        val id = optReference("id", TemplateSchema.id).uniqueIndex()
        val command = varchar("command", 255)

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id, name = "PK_template_commands_id")
    }

    object BindingsSchema : Table("template_bindings") {
        val id = optReference("id", TemplateSchema.id).uniqueIndex()
        val binding = varchar("binding", 255)

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id, name = "PK_template_bindings_id")
    }
}
