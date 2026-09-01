package com.hesham.chatting.web.auth

import com.hesham.chatting.shared.state.AuthIntent
import com.hesham.chatting.shared.state.AuthStore
import com.hesham.chatting.shared.state.AuthViewState
import kotlinx.coroutines.flow.StateFlow

interface AuthStoreBinding {
    val state: StateFlow<AuthViewState>
    fun dispatch(intent: AuthIntent)
}

class SharedAuthStoreBinding(private val store: AuthStore) : AuthStoreBinding {
    override val state: StateFlow<AuthViewState> = store.state
    override fun dispatch(intent: AuthIntent) = store.dispatch(intent)
}
