package org.readutf.orchestrator.common.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getError
import io.javalin.http.Context

typealias SResult<T> = Result<T, String>

fun <A, B> Context.result(result: Result<A, B>) {
    if (result.isErr) {
        status(500)
        val error = result.getError()
        if (error is String) {
            json(error)
        }
    } else {
        status(200)
        result.value?.let { json(it) }
    }
}

inline fun <V, E> Result<V, E>.handleFailure(handler: (E) -> Unit): V {
    if (isErr) {
        handler(error)
        error("Handler should return")
    } else {
        return value
    }
}
