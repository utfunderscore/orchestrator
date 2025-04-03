package org.readutf.orchestrator.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <T> runAsync(block: suspend CoroutineScope.() -> T) {
    runBlocking {
        async {
            block()
        }
    }
}
