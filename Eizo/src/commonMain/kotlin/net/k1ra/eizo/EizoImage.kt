package net.k1ra.eizo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.eizo.eizo.generated.resources.Res
import net.k1ra.eizo.eizo.generated.resources.error
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.cache.OptionallyEncryptedCache
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheConfiguration
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.days

private val httpClient = HoodiesNetworkClient.Builder().apply {
    cacheConfiguration = CacheEnabled(staleDataThreshold = 30.days) //We'll cache images for a long time
    retryOnConnectionFailure = true
    maxRetryLimit = 5
}.build()

expect val ioDispatcher: CoroutineDispatcher
expect fun cacheExecutor(checkLogic: suspend () -> Boolean, networkFetchLogic: () -> Unit)

@Composable
fun EizoImage(
    url: String,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.Low,
    customHeaders: Map<String, String> = mapOf(),
    customUrlQueryParams: Map<String, String> = mapOf(),
    customCacheConfiguration: CacheConfiguration? = null,
    showProgressIndicator: Boolean = true,
    fallbackPainter: Painter = painterResource(Res.drawable.error),
    fallbackModifier: Modifier? = null
) {
    var bitmap by remember { mutableStateOf(null as ImageBitmap?) }
    var isLoading by remember { mutableStateOf(true) }
    var startedLoadingImage by remember { mutableStateOf(false) }

    fun loadViaNetwork() = CoroutineScope(ioDispatcher).launch {
        //If cache missed, fetch image over the network the normal way...
        when (val result = httpClient.get<ImageBitmap>(
            url,
            customUrlQueryParams,
            customHeaders,
            customCacheConfiguration
        )) {
            is Success -> CoroutineScope(Dispatchers.Main).launch {
                bitmap = result.value
                isLoading = false
            }

            is Failure -> CoroutineScope(Dispatchers.Main).launch {
                isLoading = false
                println("ERROR: Failed to load image from $url because of ${result.reason}")
            }
        }
    }

    if (!startedLoadingImage) {
        startedLoadingImage = true

        if (url.isEmpty()) {
            isLoading = false
            println("ERROR: Failed to load image because URL is empty")

        } else {

            //Hack to get data from the HoodiesNetwork cache synchronously with no UI state changes
            val cacheConfig = customCacheConfiguration ?: httpClient.builder.cacheConfiguration
            if (cacheConfig is CacheEnabled) {
                val cache = OptionallyEncryptedCache(cacheConfig)
                val request = httpClient.buildRequestWithUrlQueryParams<ImageBitmap>(
                    "GET",
                    url,
                    customUrlQueryParams,
                    customHeaders
                )

                cacheExecutor(
                    checkLogic = {
                        if (!cache.isDataStale(request)) {
                            bitmap = httpClient.convertResponseBody<ImageBitmap>(
                                cache.getCachedData(request).data
                            )
                            isLoading = false
                            true
                        } else {
                            false
                        }
                    },
                    networkFetchLogic = { loadViaNetwork() }
                )
            } else {
                loadViaNetwork()
            }
        }
    }

    Box(
        modifier = modifier.testTag("eizoBox"),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            if (showProgressIndicator) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(0.8f).testTag("eizoProgressIndicator")
                )
            }
        } else {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize().testTag("eizoImageBitmap"),
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter,
                    filterQuality = filterQuality
                )
            } else {
                Image(
                    painter = fallbackPainter,
                    contentDescription = contentDescription,
                    modifier = (fallbackModifier ?: Modifier.fillMaxSize()).testTag("eizoFallbackImage"),
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
        }
    }
}