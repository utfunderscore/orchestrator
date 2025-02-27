package org.readutf.orchestrator.client.platform

import java.net.InetAddress

public class DockerPlatform : ContainerPlatform {
    override fun getContainerId(): String = InetAddress.getLocalHost().hostName
}
