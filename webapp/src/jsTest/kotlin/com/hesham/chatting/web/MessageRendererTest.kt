package com.hesham.chatting.web

import com.hesham.chatting.shared.state.MessageItem
import com.hesham.chatting.shared.state.MessageStatus
import com.hesham.chatting.web.chat.MessageListRenderer
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLTemplateElement
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageRendererTest {
    @AfterTest
    fun cleanDom() {
        document.body?.textContent = ""
    }

    @Test
    fun messageMarkupIsRenderedAsLiteralTextWithoutChildElements() {
        val container = document.createElement("div") as HTMLDivElement
        val template = document.createElement("template") as HTMLTemplateElement
        val row = document.createElement("article") as HTMLElement
        val bubble = document.createElement("div") as HTMLDivElement
        val text = document.createElement("p") as HTMLParagraphElement
        val retry = document.createElement("button") as HTMLButtonElement
        row.className = "message-row"
        bubble.className = "message-bubble"
        text.className = "message-text"
        retry.className = "retry-button"
        bubble.appendChild(text)
        row.appendChild(bubble)
        row.appendChild(retry)
        template.content.appendChild(row)
        document.body!!.appendChild(container)
        document.body!!.appendChild(template)

        val attack = "<img src=x onerror=alert(1)>"
        MessageListRenderer(container, template).render(
            listOf(MessageItem(null, "client-1", attack, true, null, MessageStatus.Pending)),
        )

        val renderedText = container.querySelector(".message-text") as HTMLElement
        assertEquals(attack, renderedText.textContent)
        assertEquals(0, renderedText.children.length)
        assertEquals(0, container.getElementsByTagName("img").length)
    }
}
