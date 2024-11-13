package org.readutf.orchestrator.wrapper.utils

import org.readutf.orchestrator.wrapper.OrchestratorApi

class JsonWrapper<T>(
    val data: T,
) {
    override fun toString(): String = OrchestratorApi.objectMapper.writeValueAsString(data)
}
