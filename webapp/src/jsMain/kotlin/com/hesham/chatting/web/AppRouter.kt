package com.hesham.chatting.web

import com.hesham.chatting.shared.ChattingSdk
import com.hesham.chatting.shared.auth.AuthState
import kotlinx.browser.window
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

fun interface Navigator {
    fun replace(target: String)
}

object WindowNavigator : Navigator {
    override fun replace(target: String) = window.location.replace(target)
}

class AppRouter(
    private val session: StateFlow<AuthState>,
    private val navigator: Navigator = WindowNavigator,
) {
    constructor(sdk: ChattingSdk, navigator: Navigator = WindowNavigator) : this(sdk.session, navigator)

    suspend fun guard(page: String?, onAllowed: () -> Unit = {}) {
        val authState = session.filter { it !is AuthState.Restoring }.first()
        val isAuthenticated = authState is AuthState.LoggedIn
        val destination = when {
            page == CHAT && !isAuthenticated -> LOGIN_HTML
            page in AUTH_PAGES && isAuthenticated -> CHAT_HTML
            else -> null
        }
        if (destination == null) onAllowed() else navigator.replace(destination)
    }

    companion object {
        const val REGISTER = "register"
        const val LOGIN = "login"
        const val CHAT = "chat"
        const val REGISTER_HTML = "index.html"
        const val LOGIN_HTML = "login.html"
        const val CHAT_HTML = "chat.html"
        private val AUTH_PAGES = setOf(REGISTER, LOGIN)
    }
}
