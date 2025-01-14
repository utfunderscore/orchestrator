package org.readutf.orchestrator.edge.finder

import org.readutf.orchestrator.common.utils.SResult

interface TransferFinder {
    fun findTransferAddress(): SResult<TransferAddress>
}
