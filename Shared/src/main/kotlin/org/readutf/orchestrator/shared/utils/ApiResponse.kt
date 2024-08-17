package org.readutf.orchestrator.shared.utils

class ApiResponse<T>(
    val success: Boolean,
    var failureReason: String?,
    val response: T?,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, null, data)

        fun <T> failure(reason: String): ApiResponse<T> = ApiResponse(false, reason, null)
    }

    override fun toString(): String = "ApiResponse(success=$success, failureReason=$failureReason, response=$response)"
}
