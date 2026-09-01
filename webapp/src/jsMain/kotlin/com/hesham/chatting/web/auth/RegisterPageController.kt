package com.hesham.chatting.web.auth

import com.hesham.chatting.shared.ChattingSdk
import com.hesham.chatting.shared.auth.AuthState
import com.hesham.chatting.shared.state.AuthIntent
import com.hesham.chatting.shared.state.AuthViewState
import com.hesham.chatting.web.AppRouter
import com.hesham.chatting.web.Navigator
import com.hesham.chatting.web.WindowNavigator
import com.hesham.chatting.web.dom.RegisterIds
import com.hesham.chatting.web.dom.bindStateFlow
import com.hesham.chatting.web.dom.el
import com.hesham.chatting.web.dom.on
import com.hesham.chatting.web.dom.setText
import com.hesham.chatting.web.dom.setVisible
import com.hesham.chatting.web.fieldErrorMessage
import com.hesham.chatting.web.uiErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement

class RegisterPageController(
    private val auth: AuthStoreBinding,
    private val navigator: Navigator = WindowNavigator,
    private val scope: CoroutineScope = MainScope(),
    private val successDelay: suspend (Long) -> Unit = { delay(it) },
) {
    constructor(
        sdk: ChattingSdk,
        navigator: Navigator = WindowNavigator,
        scope: CoroutineScope = MainScope(),
    ) : this(SharedAuthStoreBinding(sdk.auth), navigator, scope)

    private var redirectScheduled = false

    fun bind() {
        el<HTMLFormElement>(RegisterIds.FORM).on("submit") { event ->
            event.preventDefault()
            if (!el<HTMLInputElement>(RegisterIds.TERMS).checked) {
                renderStandaloneError(RegisterIds.TERMS_ERROR, "TERMS_REQUIRED")
                return@on
            }
            renderStandaloneError(RegisterIds.TERMS_ERROR, null)
            auth.dispatch(
                AuthIntent.Register(
                    firstName = value(RegisterIds.FIRST_NAME),
                    lastName = value(RegisterIds.LAST_NAME),
                    username = value(RegisterIds.USERNAME),
                    email = value(RegisterIds.EMAIL),
                    password = value(RegisterIds.PASSWORD),
                    confirmPassword = value(RegisterIds.CONFIRM_PASSWORD),
                ),
            )
        }
        bindPasswordToggle(RegisterIds.TOGGLE_PASSWORD, RegisterIds.PASSWORD)
        bindPasswordToggle(RegisterIds.TOGGLE_CONFIRM, RegisterIds.CONFIRM_PASSWORD)
        el<HTMLInputElement>(RegisterIds.TERMS).on("change") {
            if (el<HTMLInputElement>(RegisterIds.TERMS).checked) renderStandaloneError(RegisterIds.TERMS_ERROR, null)
        }
        el<HTMLAnchorElement>(RegisterIds.TERMS_LINK).on("click") { event ->
            event.preventDefault()
            val terms = el<HTMLInputElement>(RegisterIds.TERMS)
            terms.checked = !terms.checked
        }
        el<HTMLAnchorElement>(RegisterIds.LOGIN_LINK).on("click") { event ->
            event.preventDefault()
            navigator.replace(AppRouter.LOGIN_HTML)
        }
        scope.bindStateFlow(auth.state, ::render)
    }

    internal fun render(state: AuthViewState) {
        renderField(RegisterIds.FIRST_NAME, RegisterIds.FIRST_NAME_ERROR, state.fieldErrors["firstName"])
        renderField(RegisterIds.LAST_NAME, RegisterIds.LAST_NAME_ERROR, state.fieldErrors["lastName"])
        renderField(RegisterIds.USERNAME, RegisterIds.USERNAME_ERROR, state.fieldErrors["username"])
        renderField(RegisterIds.EMAIL, RegisterIds.EMAIL_ERROR, state.fieldErrors["email"])
        renderField(RegisterIds.PASSWORD, RegisterIds.PASSWORD_ERROR, state.fieldErrors["password"])
        renderField(RegisterIds.CONFIRM_PASSWORD, RegisterIds.CONFIRM_PASSWORD_ERROR, state.fieldErrors["confirmPassword"])
        renderStandaloneError(RegisterIds.TERMS_ERROR, state.fieldErrors["terms"])
        el<HTMLButtonElement>(RegisterIds.SUBMIT).disabled = state.isSubmitting

        val banner = el<HTMLDivElement>(RegisterIds.SUCCESS)
        val error = uiErrorMessage(state.error)
        when {
            state.authState is AuthState.LoggedIn -> {
                banner.classList.remove("error-state")
                banner.setText("✓ Account created successfully!")
                banner.setVisible(true)
                banner.classList.add("show")
                if (!redirectScheduled) {
                    redirectScheduled = true
                    scope.launch {
                        successDelay(SUCCESS_DELAY_MS)
                        navigator.replace(AppRouter.CHAT_HTML)
                    }
                }
            }
            error != null -> {
                banner.classList.add("error-state", "show")
                banner.setText(error)
                banner.setVisible(true)
            }
            else -> {
                banner.classList.remove("error-state", "show")
                banner.setVisible(false)
            }
        }
    }

    private fun bindPasswordToggle(buttonId: String, inputId: String) {
        el<HTMLButtonElement>(buttonId).on("click") {
            val input = el<HTMLInputElement>(inputId)
            val showing = input.type == "password"
            input.type = if (showing) "text" else "password"
            el<HTMLButtonElement>(buttonId).apply {
                setText(if (showing) "🙈" else "👁")
                setAttribute("aria-label", if (showing) "Hide password" else "Show password")
            }
        }
    }

    private fun value(id: String): String = el<HTMLInputElement>(id).value

    private fun renderField(inputId: String, errorId: String, code: String?) {
        el<HTMLInputElement>(inputId).classList.toggle("invalid", code != null)
        renderStandaloneError(errorId, code)
    }

    private fun renderStandaloneError(errorId: String, code: String?) {
        el<HTMLDivElement>(errorId).apply {
            setText(code?.let(::fieldErrorMessage))
            setVisible(code != null)
            classList.toggle("show", code != null)
        }
    }

    companion object {
        const val SUCCESS_DELAY_MS = 800L
    }
}
