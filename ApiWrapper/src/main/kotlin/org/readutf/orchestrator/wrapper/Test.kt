package org.readutf.orchestrator.wrapper

fun main() {
    val client = GameRequestClient("ws://localhost:9393/game/request")

    client.requestGame("test")
}
