package mockwebserver

import net.k1ra.hoodies_network_kmm.mockwebserver.MockWebServerManager

object ServerManager {
    private var server: MockWebServerManager? = null

    fun start() {
        if (server != null)
            return

        val builder = MockWebServerManager.Builder().usePort(6970)
        builder.addContext("/image/{length}", DelayedImage())
        server = builder.start()
    }
}