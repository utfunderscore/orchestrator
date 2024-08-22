package org.readutf.orchestrator.shared.utils

import com.alibaba.fastjson2.annotation.JSONField

class Result<T>(
    private val value: T?,
    private val error: String?,
) {
    fun isOk(): Boolean = error == null

    fun isError(): Boolean = error != null

    fun <U> map(f: (T) -> U): Result<U> = if (isOk()) ok(f(value!!)) else error(error!!)

    fun <U> mapTo(
        success: (T) -> U,
        failure: (String) -> U,
    ): U = if (isOk()) success(value!!) else failure(error!!)

    fun <U> flatMap(f: (T) -> Result<U>): Result<U> = if (isOk()) f(value!!) else error(error!!)

    @JSONField(serialize = false)
    fun get(): T = value!!

    @JSONField(serialize = false)
    fun getError(): String = error!!

    companion object {
        fun <T> error(error: String): Result<T> = Result(null, error)

        fun <T> ok(value: T): Result<T> = Result(value, null)
    }
}
