package org.readutf.orchestrator.server

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import org.readutf.orchestrator.shared.utils.ApiResponse

fun main() {
    Test()
}

class Test {
    init {
        val json =
            "{\"response\":[{\"IP\":\"0.0.0.0\",\"PrivatePort\":2980,\"PublicPort\":2980,\"Type\":\"tcp\"},{\"IP\":\"::\",\"PrivatePort\":2980,\"PublicPort\":2980,\"Type\":\"tcp\"},{\"IP\":\"0.0.0.0\",\"PrivatePort\":9393,\"PublicPort\":9393,\"Type\":\"tcp\"},{\"IP\":\"::\",\"PrivatePort\":9393,\"PublicPort\":9393,\"Type\":\"tcp\"}],\"success\":true}"

        val test = JSON.parseObject(json, object : TypeReference<ApiResponse<List<Map<String, String>>>>() {})

        println(test)
    }
}
