package org.readutf.orchestrator.shared.server

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.readutf.orchestrator.shared.utils.TimeUtils
import org.readutf.orchestrator.shared.utils.TypeWrapper
import java.text.DecimalFormat

open class Server(
    @JsonProperty("serverId") val serverId: String,
    @JsonProperty("address") val address: ServerAddress,
    @JsonProperty("serverType") val serverType: String,
    @JsonProperty("heartbeat") var heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    @JsonProperty("attributes") var attributes: MutableMap<String, TypeWrapper> = mutableMapOf(),
    @JsonProperty("registeredAt") val registeredAt: Long = System.currentTimeMillis(),
) {
    @JsonIgnore
    var pendingDeletion = false

    @JsonIgnore
    fun getUptime(): Long = System.currentTimeMillis() - registeredAt

    @JsonIgnore
    fun getUptimeString(): String = TimeUtils.formatDuration(getUptime())

    @JsonIgnore
    fun getShortId(): String = serverId.substring(0, 12)

    override fun toString(): String = "Server(serverId=$serverId, address=$address, heartbeat=$heartbeat, attributes=$attributes)"

    @JsonIgnore
    fun getInfoString(): String =
        "%s (uptime: %s, address: %s, lastHeartbeat: %ss, closing: %s)".format(
            getShortId(),
            getUptimeString(),
            address,
            DecimalFormat("#.##").format((System.currentTimeMillis() - heartbeat.timestamp) / 1000.0),
            pendingDeletion.toString(),
        )

    companion object {
        val PROTOCOL_ID: Byte = 0x01
    }
}
