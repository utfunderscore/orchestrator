package org.readutf.orchestrator.shared.utils

import com.fasterxml.jackson.annotation.JsonIgnore

data class ApiResponse<T>(
    val success: Boolean,
    var failureReason: String?,
    val response: T?,
) {
    @JsonIgnore
    fun isSuccess() = failureReason == null

    @JsonIgnore
    fun getError(): String = failureReason!!

    @JsonIgnore
    fun get(): T = response!!

    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, null, data)

        fun <T> failure(reason: String): ApiResponse<T> = ApiResponse(false, reason, null)
    }

    override fun toString(): String = "ApiResponse(success=$success, failureReason=$failureReason, response=$response)"
}
