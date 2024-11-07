package org.readutf.orchestrator.shared.utils

import io.github.oshai.kotlinlogging.KotlinLogging

data class Result<DATA_TYPE, ERROR_TYPE>(
    private val value: DATA_TYPE?,
    private val error: ERROR_TYPE?,
    private val causedBy: Result<*, *>? = null,
) {
    private val calledFrom: StackTraceElement = Thread.currentThread().stackTrace[4]

    fun getValue() = value!!

    fun getError() = error!!

    fun getErrorOrNull() = error

    fun throwIfFailed(): DATA_TYPE {
        if (isFailure) {
            throw IllegalStateException("Result failed: $error")
        }
        return getValue()
    }

    fun debug(context: () -> Unit) =
        apply {
            var previous: Result<*, *> = this

            val trace = mutableListOf<Result<*, *>>()
            while (true) {
                trace.add(previous)
                previous = previous.causedBy ?: break
            }
            for (result in trace.reversed()) {
                KotlinLogging.logger(context)
                logger.debug { " ↪ ${result.calledFrom.className}:${result.calledFrom.lineNumber} - ${result.error}" }
            }
        }

    fun getOrNull(): DATA_TYPE? {
        if (isSuccess) {
            return getValue()
        }
        return null
    }

    fun <U> map(mapper: (DATA_TYPE) -> U): Result<U, ERROR_TYPE> {
        if (isFailure) return failure(getError())
        return success(mapper(getValue()))
    }

    inline fun <U> mapError(supplier: (Result<U, ERROR_TYPE>) -> Unit): DATA_TYPE {
        if (isFailure) {
            debug { }
            supplier(failure(getError(), this))
        }
        return getValue()
    }

    inline fun onFailure(block: (Result<DATA_TYPE, ERROR_TYPE>) -> Unit): DATA_TYPE {
        if (isFailure) {
            block(this)
            throw IllegalStateException("Result failed: ${getErrorOrNull()}")
        }
        return this.getValue()
    }

    fun getOrThrow(): DATA_TYPE {
        if (isFailure) {
            throw IllegalStateException("Result failed: $error")
        }
        return getValue()
    }

    val isSuccess: Boolean
        get() = value != null

    val isFailure: Boolean
        get() = error != null

    companion object {
        private val logger = KotlinLogging.logger { }

        fun <T, U> success(value: T): Result<T, U> = Result(value, null)

        fun <T, U> failure(
            error: U,
            causedBy: Result<*, *>? = null,
        ): Result<T, U> = Result(null, error, causedBy)

        fun <T> fromInternal(result: kotlin.Result<T>): Result<T, String> {
            if (result.isFailure) return failure(result.exceptionOrNull()?.message ?: "null")
            return success(result.getOrNull()!!)
        }

        fun <T> empty() = success<Unit, T>(Unit)
    }
}

fun <T> Exception.toResult(): Result<T, String> = Result.failure(this.message ?: "Unknown error")

fun <T, U> T.toSuccess(): Result<T, U> = Result.success(this)

fun <T> String.toFailure(): Result<T, String> = Result.failure(this)

fun <T> kotlin.Result<T>.convert(): Result<T, String> = Result.fromInternal(this)

inline fun <T> catch(block: () -> T): Result<T, String> =
    try {
        Result.success(block())
    } catch (e: Exception) {
        e.toResult()
    }
