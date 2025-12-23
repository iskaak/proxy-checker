package org.acme.proxychecker

import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.theme.aura.Aura
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@Push
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
@SpringBootApplication
class ProxyCheckerApplication : AppShellConfigurator

fun main(args: Array<String>) {
    runApplication<ProxyCheckerApplication>(*args)
}
