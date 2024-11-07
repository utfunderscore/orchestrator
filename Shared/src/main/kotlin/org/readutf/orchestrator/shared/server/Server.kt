package org.readutf.orchestrator.shared.server

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.readutf.orchestrator.shared.utils.TimeUtils
import org.readutf.orchestrator.shared.utils.TypeWrapper
import java.text.DecimalFormat

open class Server(
    @JsonProperty("serverId") val serverId: String,
    @JsonProperty("address") val address: ServerAddress,
    @JsonProperty("address") val serverType: String,
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
    fun getShortId(): String = serverId.toString().substring(0, 8)

    override fun toString(): String = "Server(serverId=$serverId, address=$address, heartbeat=$heartbeat, attributes=$attributes)"

    fun getInfoString(): String =
        "%s (uptime: %s, address: %s, lastHeartbeat: %ss)".format(
            getShortId(),
            getUptimeString(),
            address,
            DecimalFormat("#.##").format((System.currentTimeMillis() - heartbeat.timestamp) / 1000.0),
        )

    companion object {
        val PROTOCOL_ID: Byte = 0x01
    }
}
