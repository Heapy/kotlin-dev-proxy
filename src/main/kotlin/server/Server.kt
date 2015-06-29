package server

import com.github.kevinsawicki.http.HttpRequest
import spark.Request
import spark.Response
import spark.Spark.delete
import spark.Spark.get
import spark.Spark.post
import spark.Spark.put
import spark.SparkBase.externalStaticFileLocation
import spark.SparkBase.port
import java.util.HashMap
import java.util.Properties

fun main(args: Array<String>) {
    init()

    externalStaticFileLocation(getString("static.files.location"))
    port(getInt("server.port"))

    val proxyPrefix = getString("server.proxy") + "*"

    fun getProxyUrl(req: spark.Request): String {
        return req.pathInfo().replace(getString("server.proxy"), "")
    }

    fun HttpRequest.addHeaders(req: Request): HttpRequest {
        val headers = HashMap<String, String>()
        for (header in req.headers()) {
            headers.put(header, req.headers(header))
        }
        return this.headers(headers)
    }

    fun HttpRequest.addParameters(req: Request): HttpRequest {
        for ((param, value) in req.params()) {
            this.parameter(param, value)
        }
        return this
    }

    fun HttpRequest.mapHeaders(res: Response): HttpRequest {
        for ((header, value) in this.headers()) {
            if (header != null) {
                res.header(header, value.get(0))
            }
        }
        return this
    }

    fun HttpRequest.mapStatus(res: Response): HttpRequest {
        res.status(this.code())
        return this
    }

    fun HttpRequest.result(): String {
        return this.body()
    }

    fun Go(method: String, url: String?): HttpRequest {
        when (method) {
            "GET" -> return HttpRequest.get(url)
            "POST" -> return HttpRequest.post(url)
            "PUT" -> return HttpRequest.put(url)
            "DELETE" -> return HttpRequest.delete(url)
            else -> {
                throw RuntimeException("Unexpected request method $method");
            }
        }
    }

    get(proxyPrefix, { req, res ->
        Go("GET", getProxyUrl(req)).addParameters(req).addHeaders(req).send(req.body())
                .mapHeaders(res).mapStatus(res).result()
    })

    post(proxyPrefix, { req, res ->
        Go("POST", getProxyUrl(req)).addParameters(req).addHeaders(req).send(req.body())
                .mapHeaders(res).mapStatus(res).result()
    })

    put(proxyPrefix, { req, res ->
        Go("PUT", getProxyUrl(req)).addParameters(req).addHeaders(req).send(req.body())
                .mapHeaders(res).mapStatus(res).result()
    })

    delete(proxyPrefix, { req, res ->
        Go("DELETE", getProxyUrl(req)).addParameters(req).addHeaders(req).send(req.body())
                .mapHeaders(res).mapStatus(res).result()
    })
}

val resources = Properties()
fun init() {
    resources.javaClass.getResourceAsStream("/default.properties").use {
        resources.load(it);
    }
}

fun getString(key: String): String {
    return resources.get(key)!! as String;
}

fun getInt(key: String): Int {
    return (resources.get(key)!! as String).toInt()
}
