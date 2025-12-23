package org.acme.proxychecker

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

data class Proxy(val host: String, val port: Int) {
    var status: Status = Status.UNCHECKED
    var country: String? = null

    fun check() {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CHECK_TIMEOUT_SECONDS.toLong()))
            .proxy(ProxySelector.of(InetSocketAddress(host, port)))
            .build()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://ifconfig.co/country"))
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
            .timeout(Duration.ofSeconds(CHECK_TIMEOUT_SECONDS.toLong()))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        status = if (response.statusCode() == 200) Status.SUCCEED else Status.FAILED
        country = response.body()?.trim()
    }

    enum class Status {
        UNCHECKED,
        CHECKING,
        SUCCEED,
        FAILED;

        override fun toString(): String {
            return name.lowercase().replaceFirstChar { it.uppercaseChar() }
        }
    }

    companion object {
        const val CHECK_TIMEOUT_SECONDS = 5
    }
}
