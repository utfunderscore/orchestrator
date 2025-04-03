package org.readutf.orchestrator.panel.services

import com.github.michaelbull.result.getOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.proxy.OrchestratorApi

class ServicesTask(val orchestratorApi: OrchestratorApi, val servicesChannel: ServicesChannel) : Runnable {
    private val logger = KotlinLogging.logger { }

    private var previous = emptyList<ServiceTemplate>()

    override fun run() {
        runBlocking {
            try {
                val templates = orchestratorApi.getTemplates().await().getOrThrow {
                    logger.error(it) { "Error while getting templates" }
                    return@runBlocking
                }

                if (templates != previous) {
                    logger.info { "Refreshing local templates cache" }
                    servicesChannel.updateEmbeds(templates)
                    previous = templates
                }
            } catch (e: Exception) {
                logger.error(e) { "Error while sending messages" }
            }
        }
    }
}
