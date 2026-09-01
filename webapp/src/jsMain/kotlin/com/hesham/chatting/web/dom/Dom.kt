package com.hesham.chatting.web.dom

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

inline fun <reified T : Element> el(id: String): T =
    document.getElementById(id) as? T ?: error("Expected ${T::class.simpleName} with id #$id")

fun EventTarget.on(event: String, handler: (Event) -> Unit) {
    addEventListener(event, handler)
}

fun Element.setText(value: String?) {
    textContent = value.orEmpty()
}

fun HTMLElement.setVisible(visible: Boolean) {
    hidden = !visible
}

fun <T> CoroutineScope.bindStateFlow(flow: Flow<T>, render: (T) -> Unit): Job {
    val job = launch { flow.collect(render) }
    window.addEventListener("beforeunload", { job.cancel() })
    return job
}
