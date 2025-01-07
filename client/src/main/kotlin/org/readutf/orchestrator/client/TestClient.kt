package org.readutf.orchestrator.client

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.packets.KryoBuilder
import java.net.InetAddress

fun main() {
    val logger = KotlinLogging.logger {}

    val hostAddress = System.getenv("orchestrator.hostaddress")
//    val port = System.getProperty("orchestrator.port")
    val hostname = InetAddress.getLocalHost().hostName

    logger.info { "hostAddress $hostAddress, hostName: $hostname" }

    val connectionManager =
        ConnectionManager(
            kryoPool =
                object : Pool<Kryo>(true, true) {
                    override fun create(): Kryo = KryoBuilder.build()
                },
            hostAddress = hostAddress,
            port = 2323,
            InetAddress.getLocalHost().hostName,
        )

    val connectionResult = connectionManager.connectBlocking()

    println("Result: $connectionResult")
}
