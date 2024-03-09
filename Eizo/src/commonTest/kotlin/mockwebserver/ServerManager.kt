package mockwebserver

import kotlinx.coroutines.delay
import net.k1ra.hoodies_network_kmm.mockwebserver.MockWebServerManager

object ServerManager {
    private var server: MockWebServerManager? = null

    suspend fun start() {
        val builder = MockWebServerManager.Builder()

        builder.addContext("/image/{length}", DelayedImage())

        //Sometimes the tests get run in parallel and fail because the port is already in use
        //For those cases, we will wait here until the server can start

        var started = false

        while (!started) {
            try {
                server = builder.start()
                started = true
            } catch (e: Exception) {
                delay(100)
            }
        }
    }

    fun stop() {
        server?.stop()
    }
}