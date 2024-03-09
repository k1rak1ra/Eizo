import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import mockwebserver.ServerManager
import net.k1ra.eizo.EizoImage
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.cache.OptionallyEncryptedCache
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheDisabled
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EizoTest {

    @BeforeTest
    fun setup() {
        runBlocking {
            ServerManager.start()
        }
    }

    @AfterTest
    fun stop() {
        ServerManager.stop()
    }

    @Test
    fun testLoadingImageWithProgressIndicatorNoCache() = runComposeUiTest {
        setContent {
            EizoImage(
                url = "http://localhost:6969/image/5",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                customCacheConfiguration = CacheDisabled()
            )
        }

        onNodeWithTag("eizoProgressIndicator").assertIsDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsNotDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsNotDisplayed()

        waitUntilDoesNotExist(hasTestTag("eizoProgressIndicator"), 6*1000)

        onNodeWithTag("eizoProgressIndicator").assertIsNotDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsNotDisplayed()
    }

    @Test
    fun testLoadingImageWithNoProgressIndicatorNoCache() = runComposeUiTest {
        setContent {
            EizoImage(
                url = "http://localhost:6969/image/5",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                customCacheConfiguration = CacheDisabled(),
                showProgressIndicator = false
            )
        }

        onNodeWithTag("eizoProgressIndicator").assertIsNotDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsNotDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsNotDisplayed()

        waitUntilExactlyOneExists(hasTestTag("eizoImageBitmap"), 6*1000)

        onNodeWithTag("eizoProgressIndicator").assertIsNotDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsNotDisplayed()
    }

    @Test
    fun testErrorHandling() = runComposeUiTest {
        setContent {
            EizoImage(
                url = "http://localhost:6969/doesNotExist",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                customCacheConfiguration = CacheDisabled()
            )
        }

        onNodeWithTag("eizoProgressIndicator").assertIsDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsNotDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsNotDisplayed()

        waitUntilDoesNotExist(hasTestTag("eizoProgressIndicator"), 6*1000)

        onNodeWithTag("eizoProgressIndicator").assertIsNotDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsNotDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsDisplayed()
    }

    @OptIn(InternalResourceApi::class)
    @Test
    fun testCache() = runComposeUiTest {
        val cache = OptionallyEncryptedCache(CacheEnabled())
        val url = "http://localhost:6969/fakeCachedImage"
        val request = HoodiesNetworkClient.Builder().build().buildRequestWithUrlQueryParams<ImageBitmap>("GET", url)

        runBlocking {
            cache.cacheRequestResult(readResourceBytes("files/testimage.jpg"), request)
        }

        setContent {
            EizoImage(
                url = url,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
        }

        onNodeWithTag("eizoProgressIndicator").assertIsNotDisplayed()
        onNodeWithTag("eizoImageBitmap").assertIsDisplayed()
        onNodeWithTag("eizoFallbackImage").assertIsNotDisplayed()
    }
}