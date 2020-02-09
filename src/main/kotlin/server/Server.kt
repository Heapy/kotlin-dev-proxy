package server

import io.undertow.Undertow
import io.undertow.client.UndertowClient
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.accesslog.AccessLogHandler
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient
import io.undertow.server.handlers.proxy.ProxyHandler
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import org.yaml.snakeyaml.Yaml
import java.net.URI
import java.nio.file.Paths

/**
 * Reverse proxy sample: https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/reverseproxy/ReverseProxyServer.java
 */
object Server {
    @JvmStatic
    fun main(args: Array<String>) {
        val file = javaClass.getResourceAsStream("/application.yml")
        val config = file.use { Yaml().loadAs(it, ApplicationConfig::class.java) }

        val resourceHandler = ResourceHandler(PathResourceManager(Paths.get(config.files)))

        val rootHandler = PathHandler()
            .addPrefixPath("/", resourceHandler)

        config.mappings.forEach {
            val clientProxy = LoadBalancingProxyClient(UndertowClient.getInstance())
                .addHost(URI(it.host))
            val proxyHandler = ProxyHandler.builder()
                .setProxyClient(clientProxy)
                .setRewriteHostHeader(true)
                .build()

            rootHandler.addPrefixPath(it.prefix) { exchange ->
                proxyHandler.handleRequest(exchange)
            }
        }

        Undertow.builder()
            .addHttpListener(config.port, config.host)
            .setHandler(rootHandler)
            .build()
            .start()
    }
}

