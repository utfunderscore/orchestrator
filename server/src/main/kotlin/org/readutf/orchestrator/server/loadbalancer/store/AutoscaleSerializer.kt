package org.readutf.orchestrator.server.loadbalancer.store

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.loadbalancer.Autoscaler

interface AutoscaleSerializer {

    fun save(scalers: Map<TemplateName, Autoscaler>): Result<Unit, Throwable>

    fun create(jsonNode: JsonNode): Result<Autoscaler, Throwable>

    fun load(): Result<Map<TemplateName, Autoscaler>, Throwable>
}
