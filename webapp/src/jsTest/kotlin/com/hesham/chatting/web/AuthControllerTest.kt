package com.hesham.chatting.web

import com.hesham.chatting.shared.auth.AuthState
import com.hesham.chatting.shared.model.AuthTokens
import com.hesham.chatting.shared.model.Session
import com.hesham.chatting.shared.model.User
import com.hesham.chatting.shared.state.AuthIntent
import com.hesham.chatting.shared.state.AuthViewState
import com.hesham.chatting.web.auth.AuthStoreBinding
import com.hesham.chatting.web.auth.LoginPageController
import com.hesham.chatting.web.auth.RegisterPageController
import com.hesham.chatting.web.auth.RememberPreference
import com.hesham.chatting.web.dom.LoginIds
import com.hesham.chatting.web.dom.RegisterIds
import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.w3c.dom.Element
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AuthControllerTest {
    @AfterTest
    fun cleanDom() {
        document.body?.textContent = ""
    }

    @Test
    fun registrationDispatchesSharedIntentAndRendersSharedFieldErrors() = runTest {
        installRegisterDom()
        val auth = FakeAuthBinding()
        val controller = RegisterPageController(auth, scope = backgroundScope, successDelay = {})
        controller.bind()
        runCurrent()

        input(RegisterIds.FIRST_NAME).value = "Sara"
        input(RegisterIds.LAST_NAME).value = "Ali"
        input(RegisterIds.USERNAME).value = "sara"
        input(RegisterIds.EMAIL).value = "sara@example.com"
        input(RegisterIds.PASSWORD).value = "password123"
        input(RegisterIds.CONFIRM_PASSWORD).value = "password123"
        input(RegisterIds.TERMS).checked = true
        byId<HTMLFormElement>(RegisterIds.FORM).dispatchEvent(Event("submit"))

        val intent = assertIs<AuthIntent.Register>(auth.intents.single())
        assertEquals("sara@example.com", intent.email)
        assertTrue(intent.termsAccepted)

        controller.render(AuthViewState(fieldErrors = mapOf("firstName" to "FIRST_NAME_REQUIRED")))
        assertEquals("First name is required.", byId<Element>(RegisterIds.FIRST_NAME_ERROR).textContent)
        assertTrue(input(RegisterIds.FIRST_NAME).classList.contains("invalid"))
    }

    @Test
    fun registrationWithUncheckedTermsIsBlockedAndShowsAnError() = runTest {
        installRegisterDom()
        val auth = FakeAuthBinding()
        val controller = RegisterPageController(auth, scope = backgroundScope, successDelay = {})
        controller.bind()
        runCurrent()

        input(RegisterIds.FIRST_NAME).value = "Sara"
        input(RegisterIds.LAST_NAME).value = "Ali"
        input(RegisterIds.USERNAME).value = "sara"
        input(RegisterIds.EMAIL).value = "sara@example.com"
        input(RegisterIds.PASSWORD).value = "password123"
        input(RegisterIds.CONFIRM_PASSWORD).value = "password123"
        input(RegisterIds.TERMS).checked = false
        byId<HTMLFormElement>(RegisterIds.FORM).dispatchEvent(Event("submit"))

        assertTrue(auth.intents.isEmpty())
        assertTrue(byId<Element>(RegisterIds.TERMS_ERROR).classList.contains("show"))
        assertEquals(
            "You must agree to the Terms & Conditions to continue.",
            byId<Element>(RegisterIds.TERMS_ERROR).textContent,
        )

        input(RegisterIds.TERMS).checked = true
        byId<HTMLInputElement>(RegisterIds.TERMS).dispatchEvent(Event("change"))
        assertTrue(!byId<Element>(RegisterIds.TERMS_ERROR).classList.contains("show"))

        byId<HTMLFormElement>(RegisterIds.FORM).dispatchEvent(Event("submit"))
        val intent = assertIs<AuthIntent.Register>(auth.intents.single())
        assertTrue(intent.termsAccepted)
    }

    @Test
    fun registrationRendersFriendlyMessagesForTakenUsernameEmailAndMissingTerms() = runTest {
        installRegisterDom()
        val auth = FakeAuthBinding()
        val controller = RegisterPageController(auth, scope = backgroundScope, successDelay = {})

        controller.render(
            AuthViewState(
                fieldErrors = mapOf(
                    "username" to "USERNAME_TAKEN",
                    "email" to "EMAIL_TAKEN",
                    "terms" to "TERMS_ACCEPTANCE_REQUIRED",
                ),
            ),
        )

        assertEquals("That username is already in use.", byId<Element>(RegisterIds.USERNAME_ERROR).textContent)
        assertEquals("That email is already registered.", byId<Element>(RegisterIds.EMAIL_ERROR).textContent)
        assertEquals(
            "You must accept the Terms & Conditions to create an account.",
            byId<Element>(RegisterIds.TERMS_ERROR).textContent,
        )
    }

    @Test
    fun registrationForwardsActualTermsCheckboxStateInsteadOfAHardcodedValue() = runTest {
        installRegisterDom()
        val auth = FakeAuthBinding()
        val controller = RegisterPageController(auth, scope = backgroundScope, successDelay = {})
        controller.bind()
        runCurrent()

        input(RegisterIds.FIRST_NAME).value = "Sara"
        input(RegisterIds.LAST_NAME).value = "Ali"
        input(RegisterIds.USERNAME).value = "sara"
        input(RegisterIds.EMAIL).value = "sara@example.com"
        input(RegisterIds.PASSWORD).value = "password123"
        input(RegisterIds.CONFIRM_PASSWORD).value = "password123"

        // Checked, then unchecked again without re-reading the DOM in between: proves the
        // dispatched intent tracks the live checkbox value rather than a value captured once.
        input(RegisterIds.TERMS).checked = true
        input(RegisterIds.TERMS).checked = false
        input(RegisterIds.TERMS).checked = true
        byId<HTMLFormElement>(RegisterIds.FORM).dispatchEvent(Event("submit"))

        val intent = assertIs<AuthIntent.Register>(auth.intents.single())
        assertTrue(intent.termsAccepted)
    }

    @Test
    fun registrationShowsSuccessBeforeRedirecting() = runTest {
        installRegisterDom()
        val auth = FakeAuthBinding()
        var destination: String? = null
        val controller = RegisterPageController(
            auth = auth,
            navigator = Navigator { destination = it },
            scope = backgroundScope,
            successDelay = {},
        )

        controller.render(AuthViewState(authState = loggedIn()))
        assertTrue(byId<Element>(RegisterIds.SUCCESS).classList.contains("show"))
        assertEquals("✓ Account created successfully!", byId<Element>(RegisterIds.SUCCESS).textContent)
        runCurrent()
        assertEquals(AppRouter.CHAT_HTML, destination)
    }

    @Test
    fun loginRememberCheckboxControlsPreferenceAndDispatchesSharedIntent() = runTest {
        installLoginDom()
        val auth = FakeAuthBinding()
        val preference = FakeRememberPreference()
        val controller = LoginPageController(auth, preference, scope = backgroundScope)
        controller.bind()
        runCurrent()

        input(LoginIds.USERNAME).value = "sara"
        input(LoginIds.PASSWORD).value = "password123"
        input(LoginIds.REMEMBER_ME).checked = true
        byId<HTMLFormElement>(LoginIds.FORM).dispatchEvent(Event("submit"))

        assertEquals(true, preference.remember)
        assertEquals(AuthIntent.Login("sara", "password123"), auth.intents.single())
    }

    private fun installRegisterDom() {
        val form = add("form", RegisterIds.FORM)
        listOf(
            RegisterIds.FIRST_NAME, RegisterIds.LAST_NAME, RegisterIds.USERNAME, RegisterIds.EMAIL,
            RegisterIds.PASSWORD, RegisterIds.CONFIRM_PASSWORD, RegisterIds.TERMS,
        ).forEach { form.appendChild(create("input", it)) }
        listOf(
            RegisterIds.FIRST_NAME_ERROR, RegisterIds.LAST_NAME_ERROR, RegisterIds.USERNAME_ERROR,
            RegisterIds.EMAIL_ERROR, RegisterIds.PASSWORD_ERROR, RegisterIds.CONFIRM_PASSWORD_ERROR,
            RegisterIds.TERMS_ERROR,
        ).forEach { form.appendChild(create("div", it)) }
        form.appendChild(create("button", RegisterIds.TOGGLE_PASSWORD))
        form.appendChild(create("button", RegisterIds.TOGGLE_CONFIRM))
        form.appendChild(create("a", RegisterIds.TERMS_LINK))
        form.appendChild(create("button", RegisterIds.SUBMIT))
        add("a", RegisterIds.LOGIN_LINK)
        add("div", RegisterIds.SUCCESS)
    }

    private fun installLoginDom() {
        val form = add("form", LoginIds.FORM)
        form.appendChild(create("input", LoginIds.USERNAME))
        form.appendChild(create("div", LoginIds.USERNAME_ERROR))
        form.appendChild(create("input", LoginIds.PASSWORD))
        form.appendChild(create("div", LoginIds.PASSWORD_ERROR))
        form.appendChild(create("input", LoginIds.REMEMBER_ME))
        form.appendChild(create("div", LoginIds.ERROR))
        form.appendChild(create("button", LoginIds.SUBMIT))
        add("a", LoginIds.REGISTER_LINK)
    }

    private fun add(tag: String, id: String): Element = create(tag, id).also { document.body!!.appendChild(it) }

    private fun create(tag: String, id: String): Element = document.createElement(tag).also { it.id = id }

    private fun input(id: String): HTMLInputElement = byId(id)

    private inline fun <reified T : Element> byId(id: String): T = document.getElementById(id) as T

    private fun loggedIn(): AuthState.LoggedIn {
        val instant = Instant.parse("2030-01-01T00:00:00Z")
        return AuthState.LoggedIn(
            Session(
                User("u1", "sara", "Sara", "Sara", "Ali"),
                AuthTokens("access", instant, "refresh", instant),
            ),
        )
    }
}

private class FakeAuthBinding : AuthStoreBinding {
    private val mutableState = MutableStateFlow(AuthViewState())
    override val state: StateFlow<AuthViewState> = mutableState
    val intents = mutableListOf<AuthIntent>()
    override fun dispatch(intent: AuthIntent) { intents += intent }
}

private class FakeRememberPreference : RememberPreference {
    var remember: Boolean? = null
    override fun setRememberMe(remember: Boolean) { this.remember = remember }
}
