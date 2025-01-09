package org.readutf.orchestrator.common.api

class ApiResponse<T>(
    private val success: Boolean,
    private val result: T?,
) {
    fun isSuccessful(): Boolean = success

    fun getResult(): T? = result

    companion object {
        fun <T> success(result: T): ApiResponse<T> = ApiResponse(true, result)

        fun <T> error(): ApiResponse<T> = ApiResponse(false, null)
    }
}
