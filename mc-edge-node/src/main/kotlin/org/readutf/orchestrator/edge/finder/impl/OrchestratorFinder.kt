package org.readutf.orchestrator.edge.finder.impl

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import com.github.michaelbull.result.runCatching
import org.readutf.orchestrator.edge.finder.TransferAddress
import org.readutf.orchestrator.edge.finder.TransferFinder
import org.readutf.orchestrator.proxy.OrchestratorApi

class OrchestratorFinder(
    private val orchestratorApi: OrchestratorApi,
) : TransferFinder {
    override fun findTransferAddress(): Result<TransferAddress, Throwable> = runCatching { orchestratorApi.findServerBlocking("proxy").join() }
        .flatMap { it }
        .map { server -> TransferAddress("localhost", server.networkSettings.exposedPorts.firstOrNull() ?: 25566) }
}
