package com.hesham.chatting.web

import com.hesham.chatting.shared.auth.AuthState
import com.hesham.chatting.shared.model.AuthTokens
import com.hesham.chatting.shared.model.Session
import com.hesham.chatting.shared.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class RouterGuardTest {
    @Test
    fun unauthenticatedAuthPageIsAllowed() = runTest {
        assertDecision(AppRouter.LOGIN, AuthState.LoggedOut, expectedDestination = null)
    }

    @Test
    fun unauthenticatedChatRedirectsToLogin() = runTest {
        assertDecision(AppRouter.CHAT, AuthState.LoggedOut, AppRouter.LOGIN_HTML)
    }

    @Test
    fun authenticatedAuthPageRedirectsToChat() = runTest {
        assertDecision(AppRouter.REGISTER, loggedIn(), AppRouter.CHAT_HTML)
    }

    @Test
    fun authenticatedChatIsAllowed() = runTest {
        assertDecision(AppRouter.CHAT, loggedIn(), expectedDestination = null)
    }

    private suspend fun assertDecision(page: String, state: AuthState, expectedDestination: String?) {
        var allowed = false
        var destination: String? = null
        AppRouter(MutableStateFlow(state), Navigator { destination = it }).guard(page) { allowed = true }
        assertEquals(expectedDestination, destination)
        assertEquals(expectedDestination == null, allowed)
    }

    private fun loggedIn(): AuthState.LoggedIn {
        val instant = Instant.parse("2030-01-01T00:00:00Z")
        val user = User("u1", "sara", "Sara", "Sara", "Ali")
        return AuthState.LoggedIn(Session(user, AuthTokens("access", instant, "refresh", instant)))
    }
}
