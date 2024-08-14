package org.readutf.orchestrator

import com.alibaba.fastjson2.JSON
import java.util.Random
import java.util.UUID

fun main() {
    println(
        JSON.toJSONString(
            List(10) {
                Data(UUID.randomUUID(), Random().nextFloat(), Random().nextFloat())
            },
        ),
    )
}

data class Data(
    val uuid: UUID,
    val delta_yaw: Float,
    val delta_pitch: Float,
)
