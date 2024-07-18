package org.readutf.orchestrator.server

import com.alibaba.fastjson2.JSON
import org.readutf.orchestrator.shared.utils.TypedJson

fun main() {
//    Orchestrator()

    val parseObject = JSON.parseObject("", TypedJson::class.java)
    println(parseObject)
}
