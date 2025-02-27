package org.readutf.orchestrator.edge.finder

import com.github.michaelbull.result.Result

interface TransferFinder {
    fun findTransferAddress(): Result<TransferAddress, Throwable>
}
