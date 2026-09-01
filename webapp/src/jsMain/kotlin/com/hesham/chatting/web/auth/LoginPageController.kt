package com.hesham.chatting.web.auth

import com.hesham.chatting.shared.ChattingSdk
import com.hesham.chatting.shared.auth.AuthState
import com.hesham.chatting.shared.state.AuthIntent
import com.hesham.chatting.shared.state.AuthViewState
import com.hesham.chatting.web.AppRouter
import com.hesham.chatting.web.Navigator
import com.hesham.chatting.web.WindowNavigator
import com.hesham.chatting.web.dom.LoginIds
import com.hesham.chatting.web.dom.bindStateFlow
import com.hesham.chatting.web.dom.el
import com.hesham.chatting.web.dom.on
import com.hesham.chatting.web.dom.setText
import com.hesham.chatting.web.dom.setVisible
import com.hesham.chatting.web.fieldErrorMessage
import com.hesham.chatting.web.uiErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement

class LoginPageController(
    private val auth: AuthStoreBinding,
    private val rememberPreference: RememberPreference,
    private val navigator: Navigator = WindowNavigator,
    private val scope: CoroutineScope = MainScope(),
) {
    constructor(
        sdk: ChattingSdk,
        rememberPreference: RememberPreference,
        navigator: Navigator = WindowNavigator,
        scope: CoroutineScope = MainScope(),
    ) : this(SharedAuthStoreBinding(sdk.auth), rememberPreference, navigator, scope)

    private var redirected = false

    fun bind() {
        el<HTMLFormElement>(LoginIds.FORM).on("submit") { event ->
            event.preventDefault()
            rememberPreference.setRememberMe(el<HTMLInputElement>(LoginIds.REMEMBER_ME).checked)
            auth.dispatch(AuthIntent.Login(value(LoginIds.USERNAME), value(LoginIds.PASSWORD)))
        }
        el<HTMLAnchorElement>(LoginIds.REGISTER_LINK).on("click") { event ->
            event.preventDefault()
            navigator.replace(AppRouter.REGISTER_HTML)
        }
        scope.bindStateFlow(auth.state, ::render)
    }

    internal fun render(state: AuthViewState) {
        val usernameError = state.fieldErrors["usernameOrEmail"]
            ?: state.fieldErrors["username"]
            ?: state.fieldErrors["email"]
        renderField(LoginIds.USERNAME, LoginIds.USERNAME_ERROR, usernameError)
        renderField(LoginIds.PASSWORD, LoginIds.PASSWORD_ERROR, state.fieldErrors["password"])
        el<HTMLButtonElement>(LoginIds.SUBMIT).disabled = state.isSubmitting
        val error = uiErrorMessage(state.error)
        el<HTMLDivElement>(LoginIds.ERROR).apply {
            setText(error)
            setVisible(error != null)
            classList.toggle("show", error != null)
        }
        if (state.authState is AuthState.LoggedIn && !redirected) {
            redirected = true
            navigator.replace(AppRouter.CHAT_HTML)
        }
    }

    private fun value(id: String): String = el<HTMLInputElement>(id).value

    private fun renderField(inputId: String, errorId: String, code: String?) {
        el<HTMLInputElement>(inputId).classList.toggle("invalid", code != null)
        el<HTMLDivElement>(errorId).apply {
            setText(code?.let(::fieldErrorMessage))
            setVisible(code != null)
            classList.toggle("show", code != null)
        }
    }
}
