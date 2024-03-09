import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eizoproject.eizodemo.generated.resources.Res
import eizoproject.eizodemo.generated.resources.person
import mockwebserver.ServerManager
import net.k1ra.eizo.EizoImage
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheDisabled
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    ServerManager.start()
    val imageUrl = "http://localhost:6970/image/2"

    MaterialTheme {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(16.dp))
            Text("Image that failed to load:", textAlign = TextAlign.Center)
            EizoImage(
                url = "",
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )

            Box(modifier = Modifier.height(16.dp))
            Text("Image using default cache:", textAlign = TextAlign.Center)
            EizoImage(
                url = imageUrl,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )

            Box(modifier = Modifier.height(16.dp))
            Text("Image with no caching and loading indicator:", textAlign = TextAlign.Center)
            EizoImage(
                url = imageUrl,
                modifier = Modifier.size(100.dp).clip(CircleShape),
                customCacheConfiguration = CacheDisabled()
            )

            Box(modifier = Modifier.height(16.dp))
            Text("Image with no caching and no loading indicator:", textAlign = TextAlign.Center)
            EizoImage(
                url = imageUrl,
                modifier = Modifier.size(100.dp).clip(CircleShape),
                customCacheConfiguration = CacheDisabled(),
                showProgressIndicator = false
            )

            Box(modifier = Modifier.height(16.dp))
            Text("Image that failed to load with custom small fallback drawable:", textAlign = TextAlign.Center)
            EizoImage(
                url = "",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                fallbackPainter = painterResource(Res.drawable.person),
                fallbackModifier = Modifier.size(50.dp)
            )
        }
    }
}