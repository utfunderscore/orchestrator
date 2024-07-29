package org.readutf.orchestrator.server.server.store.impl.h2

import org.jetbrains.exposed.sql.Table

object Server : Table() {
    val id = uuid("id")
    val serverAddress = varchar("serverAdress", 255)
    val serverPort = integer("serverPort")

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "PK_Server_ID")
}

object ServerSupportedGameType : Table() {
    val id = uuid("id").autoGenerate()
    val serverId = uuid("serverId") references Server.id
    val type = varchar("type", 50)

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "PK_ServerSupportedGameType_ID")
}
