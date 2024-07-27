package org.readutf.orchestrator.integration

import com.alibaba.fastjson2.JSON
import org.readutf.orchestrator.shared.game.GameRequest
import java.util.*

fun main() {
    println(
        JSON.toJSONString(
            GameRequest(
                UUID.randomUUID(),
                "test",
            ),
        ),
    )
}
