package org.readutf.orchestrator.common.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.runCatching
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

inline fun <reified T> JsonNode?.convert(objectMapper: ObjectMapper): Result<T, Throwable> {
    if (this == null) return Err(NullPointerException())
    return runCatching {
        objectMapper.convertValue<T>(this)
    }
}
