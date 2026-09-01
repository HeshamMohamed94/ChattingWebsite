package com.hesham.chatting.web.chat

import com.hesham.chatting.shared.model.Conversation
import com.hesham.chatting.shared.state.MessageItem
import com.hesham.chatting.shared.state.MessageStatus
import com.hesham.chatting.web.dom.ChatIds
import com.hesham.chatting.web.dom.el
import org.w3c.dom.DocumentFragment
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTemplateElement
import kotlin.time.Clock
import kotlin.time.Instant

class ConversationListRenderer(
    private val container: HTMLElement = el(ChatIds.CONVERSATION_LIST),
    private val template: HTMLTemplateElement = el(ChatIds.CONVERSATION_ROW_TEMPLATE),
) {
    fun render(conversations: List<Conversation>, selectedId: String?, onOpen: (String) -> Unit) {
        container.clearChildren()
        conversations.forEach { conversation ->
            val fragment = template.content.cloneNode(deep = true) as DocumentFragment
            val row = fragment.required<HTMLButtonElement>(".conversation-row")
            val participant = conversation.otherParticipant
            val name = "${participant.firstName} ${participant.lastName}".trim()
            fragment.required<HTMLElement>(".avatar").textContent = name.firstOrNull()?.uppercase() ?: "?"
            fragment.required<HTMLElement>(".conversation-name").textContent = name
            fragment.required<HTMLElement>(".conversation-preview").textContent = conversation.lastMessage?.text ?: "No messages yet"
            fragment.required<HTMLElement>(".conversation-time").textContent =
                relativeTimestamp(conversation.lastMessage?.createdAt ?: conversation.lastActivityAt)
            row.classList.toggle("active", conversation.id == selectedId)
            row.setAttribute("aria-current", if (conversation.id == selectedId) "true" else "false")
            row.addEventListener("click", { onOpen(conversation.id) })
            container.appendChild(fragment)
        }
    }
}

class MessageListRenderer(
    private val container: HTMLElement = el(ChatIds.MESSAGE_LIST),
    private val template: HTMLTemplateElement = el(ChatIds.MESSAGE_BUBBLE_TEMPLATE),
) {
    fun render(messages: List<MessageItem>, onRetry: (String) -> Unit = {}) {
        container.clearChildren()
        messages.forEach { message ->
            val fragment = template.content.cloneNode(deep = true) as DocumentFragment
            val row = fragment.required<HTMLElement>(".message-row")
            val textNode = fragment.required<HTMLElement>(".message-text")
            val retry = fragment.required<HTMLButtonElement>(".retry-button")

            // Security boundary: message text is always assigned as text, never parsed as markup.
            textNode.textContent = message.text
            row.classList.toggle("mine", message.isMine)
            row.classList.toggle("pending", message.status == MessageStatus.Pending)
            row.setAttribute("data-message-id", message.clientMessageId)
            retry.hidden = message.status != MessageStatus.Failed
            retry.addEventListener("click", { onRetry(message.clientMessageId) })
            container.appendChild(fragment)
        }
    }
}

private fun HTMLElement.clearChildren() {
    while (firstChild != null) removeChild(firstChild!!)
}

private inline fun <reified T : Element> DocumentFragment.required(selector: String): T =
    querySelector(selector) as? T ?: error("Template is missing $selector")

private fun relativeTimestamp(instant: Instant, now: Instant = Clock.System.now()): String {
    val seconds = (now - instant).inWholeSeconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "now"
        seconds < 3_600 -> "${seconds / 60}m"
        seconds < 86_400 -> "${seconds / 3_600}h"
        seconds < 604_800 -> "${seconds / 86_400}d"
        else -> "${seconds / 604_800}w"
    }
}
