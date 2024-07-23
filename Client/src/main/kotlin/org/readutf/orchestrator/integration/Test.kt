package org.readutf.orchestrator.integration

import com.alibaba.fastjson2.JSON
import java.util.*
import org.readutf.orchestrator.shared.game.GameRequest

fun main() {

    println(
        JSON.toJSONString(
            GameRequest(
                UUID.randomUUID(),
                "test",
                2,
                1,
            ),
        ),
    )
}
