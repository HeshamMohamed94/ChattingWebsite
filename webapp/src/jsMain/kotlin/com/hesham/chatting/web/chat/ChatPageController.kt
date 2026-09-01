package com.hesham.chatting.web.chat

import com.hesham.chatting.shared.ChattingSdk
import com.hesham.chatting.shared.auth.AuthState
import com.hesham.chatting.shared.model.User
import com.hesham.chatting.shared.realtime.ConnectionState
import com.hesham.chatting.shared.state.ChatIntent
import com.hesham.chatting.shared.state.ChatListIntent
import com.hesham.chatting.shared.state.ChatListViewState
import com.hesham.chatting.shared.state.ChatStore
import com.hesham.chatting.shared.state.ChatViewState
import com.hesham.chatting.shared.state.NewChatIntent
import com.hesham.chatting.shared.state.NewChatViewState
import com.hesham.chatting.web.AppRouter
import com.hesham.chatting.web.Navigator
import com.hesham.chatting.web.WindowNavigator
import com.hesham.chatting.web.dom.ChatIds
import com.hesham.chatting.web.dom.bindStateFlow
import com.hesham.chatting.web.dom.el
import com.hesham.chatting.web.dom.on
import com.hesham.chatting.web.dom.setText
import com.hesham.chatting.web.dom.setVisible
import com.hesham.chatting.web.uiErrorMessage
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLTextAreaElement

class ChatPageController(
    private val sdk: ChattingSdk,
    private val navigator: Navigator = WindowNavigator,
    private val scope: CoroutineScope = MainScope(),
) {
    private val conversationRenderer = ConversationListRenderer()
    private val messageRenderer = MessageListRenderer()
    private var activeConversationId: String? = null
    private var activeChat: ChatStore? = null
    private var activeChatJob: Job? = null
    private var newChatJob: Job? = null
    private var showingNewChat = false

    fun bind() {
        el<HTMLButtonElement>(ChatIds.NEW_CHAT_BUTTON).on("click") { openNewChat() }
        el<HTMLButtonElement>(ChatIds.CLOSE_NEW_CHAT).on("click") { closePane() }
        el<HTMLButtonElement>(ChatIds.BACK_TO_CONVERSATIONS).on("click") { closePane() }
        el<HTMLButtonElement>(ChatIds.LOGOUT_BUTTON).on("click") {
            sdk.auth.dispatch(com.hesham.chatting.shared.state.AuthIntent.Logout)
        }
        el<HTMLButtonElement>(ChatIds.LOAD_MORE).on("click") { activeChat?.dispatch(ChatIntent.LoadMore) }
        el<HTMLFormElement>(ChatIds.COMPOSER_FORM).on("submit") { event ->
            event.preventDefault()
            activeChat?.dispatch(ChatIntent.SendMessage)
        }
        el<HTMLTextAreaElement>(ChatIds.COMPOSER_INPUT).on("input") { event ->
            val text = (event.target as HTMLTextAreaElement).value
            activeChat?.dispatch(ChatIntent.ComposerChanged(text))
        }
        el<HTMLInputElement>(ChatIds.USER_SEARCH_INPUT).on("input") { event ->
            val text = (event.target as HTMLInputElement).value
            currentNewChatStore?.dispatch(NewChatIntent.QueryChanged(text))
        }
        window.addEventListener("hashchange", { openFromHash() })

        scope.bindStateFlow(sdk.chatList.state, ::renderConversationList)
        scope.bindStateFlow(sdk.session) { state ->
            if (state is AuthState.LoggedOut) navigator.replace(AppRouter.LOGIN_HTML)
        }
        sdk.chatList.dispatch(ChatListIntent.Refresh)
        openFromHash()
    }

    private var currentNewChatStore: com.hesham.chatting.shared.state.NewChatStore? = null

    private fun openFromHash() {
        if (showingNewChat) return
        val conversationId = window.location.hash.removePrefix("#").removePrefix("c/").takeIf {
            window.location.hash.startsWith("#c/") && it.isNotBlank()
        }
        if (conversationId == null) closePane(updateHash = false) else openConversation(conversationId, updateHash = false)
    }

    private fun openConversation(conversationId: String, updateHash: Boolean = true) {
        showingNewChat = false
        if (activeConversationId != conversationId) {
            activeConversationId?.let(sdk::closeChat)
            activeChatJob?.cancel()
            activeConversationId = conversationId
            activeChat = sdk.chat(conversationId).also { store ->
                activeChatJob = scope.bindStateFlow(store.state, ::renderThread)
                store.dispatch(ChatIntent.LoadInitial)
            }
        }
        newChatJob?.cancel()
        el<HTMLElement>(ChatIds.NEW_CHAT_PANE).setVisible(false)
        el<HTMLElement>(ChatIds.NO_CONVERSATION_SELECTED).setVisible(false)
        el<HTMLElement>(ChatIds.THREAD).setVisible(true)
        el<HTMLElement>(ChatIds.SHELL).classList.add("pane-open")
        if (updateHash && window.location.hash != "#c/$conversationId") window.location.hash = "c/$conversationId"
        renderConversationList(sdk.chatList.state.value)
    }

    private fun closePane(updateHash: Boolean = true) {
        showingNewChat = false
        newChatJob?.cancel()
        el<HTMLElement>(ChatIds.NEW_CHAT_PANE).setVisible(false)
        el<HTMLElement>(ChatIds.THREAD).setVisible(false)
        el<HTMLElement>(ChatIds.NO_CONVERSATION_SELECTED).setVisible(true)
        el<HTMLElement>(ChatIds.SHELL).classList.remove("pane-open")
        if (updateHash && window.location.hash.isNotEmpty()) window.location.hash = ""
    }

    private fun openNewChat() {
        showingNewChat = true
        el<HTMLElement>(ChatIds.THREAD).setVisible(false)
        el<HTMLElement>(ChatIds.NO_CONVERSATION_SELECTED).setVisible(false)
        el<HTMLElement>(ChatIds.NEW_CHAT_PANE).setVisible(true)
        el<HTMLElement>(ChatIds.SHELL).classList.add("pane-open")
        if (window.location.hash.isNotEmpty()) window.location.hash = ""
        currentNewChatStore = sdk.newChat().also { store ->
            newChatJob?.cancel()
            newChatJob = scope.bindStateFlow(store.state, ::renderNewChat)
        }
        el<HTMLInputElement>(ChatIds.USER_SEARCH_INPUT).value = ""
        el<HTMLInputElement>(ChatIds.USER_SEARCH_INPUT).focus()
    }

    private fun renderConversationList(state: ChatListViewState) {
        conversationRenderer.render(state.conversations, activeConversationId, ::openConversation)
        el<HTMLElement>(ChatIds.CONVERSATION_LOADING).setVisible(state.isLoading && state.conversations.isEmpty())
        el<HTMLElement>(ChatIds.NO_CONVERSATIONS).setVisible(!state.isLoading && state.conversations.isEmpty())
        renderError(ChatIds.CONVERSATION_ERROR, uiErrorMessage(state.error))
        el<HTMLParagraphElement>(ChatIds.CONNECTION_STATUS).setText(connectionText(state.connection))
    }

    private fun renderThread(state: ChatViewState) {
        messageRenderer.render(state.messages) { clientMessageId -> activeChat?.dispatch(ChatIntent.Retry(clientMessageId)) }
        val participant = state.otherParticipant
        el<HTMLElement>(ChatIds.THREAD_TITLE).setText(participant?.displayName() ?: "Chat")
        el<HTMLElement>(ChatIds.THREAD_SUBTITLE).setText(connectionText(state.connection).ifBlank { "Online" })
        el<HTMLElement>(ChatIds.NO_MESSAGES).setVisible(!state.isLoadingInitial && state.messages.isEmpty())
        el<HTMLButtonElement>(ChatIds.LOAD_MORE).apply {
            setVisible(state.hasMore && state.messages.isNotEmpty())
            disabled = state.isLoadingMore
            setText(if (state.isLoadingMore) "Loading…" else "Load earlier messages")
        }
        el<HTMLTextAreaElement>(ChatIds.COMPOSER_INPUT).apply {
            if (value != state.composerText) value = state.composerText
        }
        el<HTMLButtonElement>(ChatIds.SEND).disabled = state.composerText.isBlank() || state.isSending
        renderError(ChatIds.THREAD_ERROR, uiErrorMessage(state.error))
    }

    private fun renderNewChat(state: NewChatViewState) {
        val status = when {
            state.isSearching -> "Searching…"
            state.query.isNotBlank() && state.results.isEmpty() -> "No users found"
            else -> ""
        }
        el<HTMLElement>(ChatIds.USER_SEARCH_STATUS).setText(status)
        renderError(ChatIds.NEW_CHAT_ERROR, uiErrorMessage(state.error))
        renderUsers(state.results, state.isCreating)
        state.createdConversationId?.let { conversationId ->
            currentNewChatStore?.dispatch(NewChatIntent.ConsumeNavigation)
            openConversation(conversationId)
        }
    }

    private fun renderUsers(users: List<User>, disabled: Boolean) {
        val container = el<HTMLDivElement>(ChatIds.USER_SEARCH_RESULTS)
        while (container.firstChild != null) container.removeChild(container.firstChild!!)
        users.forEach { user ->
            val button = document.createElement("button") as HTMLButtonElement
            val copy = document.createElement("div") as HTMLDivElement
            val name = document.createElement("strong") as HTMLElement
            val username = document.createElement("span") as HTMLElement
            button.type = "button"
            button.className = "user-result"
            button.disabled = disabled
            name.textContent = user.displayName()
            username.textContent = "@${user.usernameDisplay}"
            copy.appendChild(name)
            copy.appendChild(username)
            button.appendChild(copy)
            button.addEventListener("click", { currentNewChatStore?.dispatch(NewChatIntent.Select(user.id)) })
            container.appendChild(button)
        }
    }

    private fun renderError(id: String, message: String?) {
        el<HTMLElement>(id).apply {
            setText(message)
            setVisible(message != null)
        }
    }

    private fun User.displayName(): String = "$firstName $lastName".trim()

    private fun connectionText(connection: ConnectionState): String = when (connection) {
        ConnectionState.Connected -> ""
        ConnectionState.Connecting -> "Connecting…"
        is ConnectionState.Reconnecting -> "Reconnecting (attempt ${connection.attempt})"
        ConnectionState.Offline -> "Offline"
    }
}
