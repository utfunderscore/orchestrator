package org.readutf.orchestrator.common.api

data class ApiResponse<T>(
    val success: Boolean,
    val result: T?,
    val failureMessage: String? = null,
) {
    companion object {
        fun <T> success(result: T): ApiResponse<T> = ApiResponse(
            success = true,
            result = result,
            failureMessage = null,
        )

        fun error(failureReason: String): ApiResponse<Nothing> = ApiResponse(
            success = false,
            result = null,
            failureMessage = failureReason,
        )
    }
}
