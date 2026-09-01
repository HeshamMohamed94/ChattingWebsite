package com.hesham.chatting.web

import com.hesham.chatting.shared.model.Conversation
import com.hesham.chatting.shared.model.User
import com.hesham.chatting.web.chat.ConversationListRenderer
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTemplateElement
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ConversationRendererTest {
    @AfterTest
    fun cleanDom() {
        document.body?.textContent = ""
    }

    @Test
    fun rerenderUsesLatestStoreOrderWithoutUiSorting() {
        val container = document.createElement("div") as HTMLDivElement
        val template = document.createElement("template") as HTMLTemplateElement
        val row = document.createElement("button") as HTMLButtonElement
        row.className = "conversation-row"
        listOf("avatar", "conversation-name", "conversation-preview", "conversation-time").forEach { className ->
            row.appendChild((document.createElement("span") as HTMLSpanElement).apply { this.className = className })
        }
        template.content.appendChild(row)
        val renderer = ConversationListRenderer(container, template)
        val first = conversation("first", "First")
        val second = conversation("second", "Second")

        renderer.render(listOf(first, second), null) {}
        assertEquals(listOf("First User", "Second User"), renderedNames(container))

        renderer.render(listOf(second, first), null) {}
        assertEquals(listOf("Second User", "First User"), renderedNames(container))
    }

    private fun renderedNames(container: HTMLElement): List<String> {
        val nodes = container.querySelectorAll(".conversation-name")
        return List(nodes.length) { index -> nodes.item(index)?.textContent.orEmpty() }
    }

    private fun conversation(id: String, firstName: String): Conversation = Conversation(
        id = id,
        otherParticipant = User("user-$id", id, id, firstName, "User"),
        lastMessage = null,
        lastActivityAt = Instant.parse("2030-01-01T00:00:00Z"),
    )
}
