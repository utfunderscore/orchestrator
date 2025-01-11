package org.readutf.orchestrator.edge.finder.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.edge.finder.TransferAddress
import org.readutf.orchestrator.edge.finder.TransferFinder

class StaticFinder(
    private val transferAddress: TransferAddress,
) : TransferFinder {
    private val logger = KotlinLogging.logger { }

    init {
        logger.info { "Using static transfer address: $transferAddress" }
    }

    override fun findTransferAddress(): TransferAddress = transferAddress
}
