package net.k1ra.eizo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual val ioDispatcher: CoroutineDispatcher
    get() = Dispatchers.Default

actual fun cacheExecutor(
    checkLogic: suspend () -> Boolean,
    networkFetchLogic: () -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        if (!checkLogic.invoke())
            networkFetchLogic.invoke()
    }
}