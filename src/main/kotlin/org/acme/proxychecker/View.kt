package org.acme.proxychecker

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.router.Route
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

@Route("")
class View : VerticalLayout(FlexComponent.Alignment.CENTER) {

    private val proxiesTextArea = TextArea("Proxies", """
        127.0.0.1:8080
        example.com:443
    """.trimIndent())
    private val statusSpan = Span()
    private val checkButton = Button("Check")
    private val proxiesGrid = Grid(Proxy::class.java)

    private var thread: Thread? = null

    init {
        add(proxiesTextArea, statusSpan, checkButton, proxiesGrid)

        checkButton.addClickListener {
            thread?.interrupt()

            val ui = UI.getCurrent()

            thread = Thread.startVirtualThread {
                val proxies = proxiesTextArea.value.trim().lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { it.split(":") }
                    .mapNotNull {
                        try {
                            Proxy(it[0], it[1].toInt())
                        } catch (_: Exception) {
                            null
                        }
                    }.toList()

                ui.access {
                    proxiesGrid.setItems(proxies)
                    proxiesGrid.isVisible = true
                }

                val semaphore = Semaphore(MAX_THREADS)

                Executors.newVirtualThreadPerTaskExecutor().use { executor ->
                    val size = proxies.size
                    val i = AtomicInteger(0)

                    proxies.forEach { proxy ->
                        executor.submit {
                            try {
                                semaphore.acquire()
                                ui.access { statusSpan.text = "Checking proxies [${i.incrementAndGet()}/${size}]" }
                                proxy.status = Proxy.Status.CHECKING
                                ui.access { proxiesGrid.dataProvider.refreshItem(proxy) }
                                try {
                                    proxy.check()
                                } catch (_: Exception) {
                                    proxy.status = Proxy.Status.FAILED
                                } finally {
                                    ui.access { proxiesGrid.dataProvider.refreshItem(proxy) }
                                }
                            } finally {
                                semaphore.release()
                            }
                        }
                    }
                }

                ui.access {
                    statusSpan.text = "Checked all proxies"
                }
            }
        }

        proxiesGrid.apply {
            isVisible = false
            partNameGenerator = SerializableFunction { it.status.name }
        }
    }

    companion object {
        const val MAX_THREADS = 3
    }
}
