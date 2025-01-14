package org.readutf.orchestrator.server.utils

import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class UUIDGeneratorV1 {
    fun generate(): UUID {
        val timestamp = getTimestamp()
        val clockSeqAndNode = getClockSeqAndNode()

        var mostSigBits = (timestamp and 0xFFFFFFFFFFFF0L) shl 4 // 48 bits of timestamp
        mostSigBits = mostSigBits or (0x1L shl 12) // Version 1
        mostSigBits = mostSigBits or ((timestamp ushr 48) and 0x0FFF) // High 12 bits of timestamp

        return UUID(mostSigBits, clockSeqAndNode)
    }

    private fun getTimestamp(): Long {
        // UUID timestamp starts from Oct 15, 1582.
        val uuidEpoch = -12219292800000L // in milliseconds
        val currentTime = System.currentTimeMillis()
        val nanosSinceUUIDEpoch = (currentTime - uuidEpoch) * 10000
        return nanosSinceUUIDEpoch
    }

    private fun getClockSeqAndNode(): Long {
        val random = ThreadLocalRandom.current()

        // 14 bits for the clock sequence
        var clockSeq = random.nextInt(1 shl 14).toLong()
        clockSeq = clockSeq or 0x8000 // Set variant to IETF RFC 4122

        // 48 bits for the node (MAC address or random)
        var node = getHardwareAddress()
        if (node == 0L) {
            node = random.nextLong() and 0xFFFFFFFFFFFFL // Random 48-bit value
            node = node or 0x010000000000L // Set multicast bit
        }

        return (clockSeq shl 48) or node
    }

    private fun getHardwareAddress(): Long {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (network in interfaces) {
                if (!network.isLoopback && network.hardwareAddress != null) {
                    val mac = network.hardwareAddress
                    val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
                    buffer.put(ByteArray(2)) // Padding for 48 bits to fit into 64 bits
                    buffer.put(mac)
                    buffer.flip()
                    return buffer.long
                }
            }
        } catch (_: Exception) {
        }
        return 0 // Return 0 if unable to get MAC address
    }
}
