package server

import com.github.kevinsawicki.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Content
import org.apache.http.client.fluent.Request
import org.apache.http.client.fluent.Response
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import spark.Spark.delete
import spark.Spark.get
import spark.Spark.post
import spark.Spark.put
import spark.SparkBase.externalStaticFileLocation
import spark.SparkBase.port
import java.nio.file.Files
import java.util.HashMap
import java.util.Properties

fun main(args: Array<String>) {
    init()

    // Server setup

    externalStaticFileLocation(getString("static.files.location"))
    port(getInt("server.port"))

    val proxyPrefix = getString("server.proxy") + "*"

    fun url(req: spark.Request): String {
        val proxyUrl = req.pathInfo().replace(getString("server.proxy"), "")
        return if (req.queryString() === null) proxyUrl else proxyUrl + "?" + req.queryString()
    }

    // Extensions for library

    fun Request.addHeaders(req: spark.Request): Request {
        req.headers().filter { it -> it !== "Content-Length" } forEach {
            this.setHeader(it, req.headers(it))
        }
        return this
    }

    fun Request.addBody(req: spark.Request): Request {
        return this.bodyByteArray(req.bodyAsBytes())
    }

    fun Request.go(): HttpResponse {
        return this.execute().returnResponse()
    }

    fun HttpResponse.mapHeaders(res: spark.Response): HttpResponse {
        this.getAllHeaders().forEach {
            res.header(it.getName(), it.getValue())
        }
        return this
    }

    fun HttpResponse.mapStatus(res: spark.Response): HttpResponse {
        res.status(this.getStatusLine().getStatusCode())
        return this
    }

    fun HttpResponse.result(): String {
        val entity = this.getEntity()
        return if (entity === null) "" else String(EntityUtils.toByteArray(entity))
    }

    // Actually proxy

    get(proxyPrefix, { req, res ->
        Request.Get(url(req)).addHeaders(req)
                .go()
                .mapHeaders(res).mapStatus(res)
                .result()
    })

    post(proxyPrefix, { req, res ->
        Request.Post(url(req)).addHeaders(req).addBody(req)
                .go()
                .mapHeaders(res).mapStatus(res)
                .result()
    })

    put(proxyPrefix, { req, res ->
        Request.Put(url(req)).addHeaders(req).addBody(req)
                .go()
                .mapHeaders(res).mapStatus(res)
                .result()
    })

    delete(proxyPrefix, { req, res ->
        Request.Delete(url(req)).addHeaders(req)
                .go()
                .mapHeaders(res).mapStatus(res)
                .result()
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
