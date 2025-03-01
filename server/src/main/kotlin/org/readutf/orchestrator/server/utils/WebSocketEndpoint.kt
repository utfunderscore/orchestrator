package org.readutf.orchestrator.server.utils

import io.javalin.Javalin
import io.javalin.websocket.WsBinaryMessageContext
import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsErrorContext
import io.javalin.websocket.WsMessageContext

interface WebSocketEndpoint {

    fun handleBinaryMessage(ctx: WsBinaryMessageContext) {}

    fun handleConnect(ctx: WsConnectContext) {}

    fun handleMessage(ctx: WsMessageContext) {}

    fun handleClose(ctx: WsCloseContext) {}

    fun handleError(ctx: WsErrorContext) {}
}

fun Javalin.ws(path: String, endpoint: WebSocketEndpoint) {
    ws(path) { ctx ->
        ctx.onBinaryMessage(endpoint::handleBinaryMessage)
        ctx.onConnect(endpoint::handleConnect)
        ctx.onMessage(endpoint::handleMessage)
        ctx.onClose(endpoint::handleClose)
        ctx.onError(endpoint::handleError)
    }
}
