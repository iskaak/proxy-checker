package org.acme.proxychecker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProxyCheckerApplication

fun main(args: Array<String>) {
    runApplication<ProxyCheckerApplication>(*args)
}
