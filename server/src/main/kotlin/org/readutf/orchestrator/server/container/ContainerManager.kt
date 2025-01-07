package org.readutf.orchestrator.server.container

class ContainerManager<T : ContainerTemplate>(
    private val containerController: ContainerController<T>,
)
