package net.k1ra.eizo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking

actual val ioDispatcher: CoroutineDispatcher
    get() = Dispatchers.IO

actual fun cacheExecutor(
    checkLogic: suspend () -> Boolean,
    networkFetchLogic: () -> Unit
) {
    if (!runBlocking { checkLogic.invoke() }) {
        networkFetchLogic.invoke()
    }
}