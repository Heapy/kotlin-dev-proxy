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

    fun getProxyUrl(req: Request): String {
        return req.pathInfo().replace(getString("server.proxy"), "")
    }

    fun mapReqHeaders(req: Request): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        for (header in req.headers()) {
            headers.put(header, req.headers(header))
        }
        return headers
    }

    fun mapResponse(res: Response, req: HttpRequest): String {
        for ((header, value) in req.headers()) {
            if (header != null) {
                res.header(header, value.get(0))
            }
        }
        return req.body()
    }

    get(proxyPrefix, { req, res ->
        mapResponse(res, HttpRequest.get(getProxyUrl(req)).headers(mapReqHeaders(req)).send(req.body()))
    })

    post(proxyPrefix, { req, res ->
        mapResponse(res, HttpRequest.post(getProxyUrl(req)).headers(mapReqHeaders(req)).send(req.body()))
    })

    put(proxyPrefix, { req, res ->
        mapResponse(res, HttpRequest.put(getProxyUrl(req)).headers(mapReqHeaders(req)).send(req.body()))
    })

    delete(proxyPrefix, { req, res ->
        mapResponse(res, HttpRequest.delete(getProxyUrl(req)).headers(mapReqHeaders(req)).send(req.body()))
    })

    post("/", { req, res ->
        req.body()
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
