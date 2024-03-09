package mockwebserver

import kotlinx.coroutines.delay
import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

class DelayedImage : WebServerHandler() {
    @OptIn(InternalResourceApi::class)
    override suspend fun handleRequest(call: HttpCall) {
        get {
            val delayLength = call.getCallArguments()["length"]!!

            delay(delayLength.toLong() * 1000L)

            call.respond(200, readResourceBytes("files/testimage.jpg"))
        }
    }

}