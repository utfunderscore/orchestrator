package org.readutf.orchestrator.shared.utils

import com.alibaba.fastjson2.annotation.JSONField

class ApiResponse<T>(
    val success: Boolean,
    var failureReason: String?,
    val response: T?,
) {
    @JSONField(serialize = false)
    fun isSuccess() = failureReason == null

    @JSONField(serialize = false)
    fun getError(): String = failureReason!!

    @JSONField(serialize = false)
    fun get(): T = response!!

    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, null, data)

        fun <T> failure(reason: String): ApiResponse<T> = ApiResponse(false, reason, null)
    }

    override fun toString(): String = "ApiResponse(success=$success, failureReason=$failureReason, response=$response)"
}
