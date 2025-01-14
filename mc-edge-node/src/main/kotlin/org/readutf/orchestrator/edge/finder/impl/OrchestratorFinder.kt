package org.readutf.orchestrator.edge.finder.impl

import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.edge.finder.TransferAddress
import org.readutf.orchestrator.edge.finder.TransferFinder
import org.readutf.orchestrator.proxy.OrchestratorApi

class OrchestratorFinder(
    private val orchestratorApi: OrchestratorApi,
) : TransferFinder {
    override fun findTransferAddress(): SResult<TransferAddress> =
        runCatching { orchestratorApi.findServerBlocking("proxy").join() }
            .flatMap { it }
            .map { server -> TransferAddress("localhost", server.networkSettings.exposedPorts.firstOrNull() ?: 25566) }
            .mapError { it.toString() }
}
