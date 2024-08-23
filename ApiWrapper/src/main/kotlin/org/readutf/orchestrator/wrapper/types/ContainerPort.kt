package org.readutf.orchestrator.wrapper.types

import com.alibaba.fastjson2.annotation.JSONField

data class ContainerPort(
    @JSONField(name = "IP") val ip: String,
    @JSONField(name = "PrivatePort") val privatePort: Int,
    @JSONField(name = "PublicPort") val publicPort: Int,
    @JSONField(name = "Type") val type: String,
)
