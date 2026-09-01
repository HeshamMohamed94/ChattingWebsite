package com.hesham.chatting.web.auth

import com.hesham.chatting.shared.auth.TokenStorage
import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage

interface RememberPreference {
    fun setRememberMe(remember: Boolean)
}

interface RefreshTokenPersistence {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}

private object LocalRefreshTokenPersistence : RefreshTokenPersistence {
    override fun read(key: String): String? = localStorage.getItem(key)
    override fun write(key: String, value: String) = localStorage.setItem(key, value)
    override fun remove(key: String) = localStorage.removeItem(key)
}

private object SessionRefreshTokenPersistence : RefreshTokenPersistence {
    override fun read(key: String): String? = sessionStorage.getItem(key)
    override fun write(key: String, value: String) = sessionStorage.setItem(key, value)
    override fun remove(key: String) = sessionStorage.removeItem(key)
}

/**
 * This app ships as separate static HTML pages (register/login/chat), each a fresh JS
 * bootstrap with no shared in-memory state. A refresh token that isn't durably stored
 * somewhere never survives the navigation from index.html/login.html to chat.html, so
 * every session - remembered or not - must land in one of the two browser storage tiers.
 */
class WebTokenStorage(
    private val persistent: RefreshTokenPersistence = LocalRefreshTokenPersistence,
    private val tabLifetime: RefreshTokenPersistence = SessionRefreshTokenPersistence,
) : TokenStorage, RememberPreference {
    private var refreshTokenInMemory: String? = null
    private var persistRefreshToken = false

    override fun setRememberMe(remember: Boolean) {
        persistRefreshToken = remember
    }

    override suspend fun readRefreshToken(): String? {
        refreshTokenInMemory?.let { return it }
        persistent.read(KEY)?.let { restored ->
            refreshTokenInMemory = restored
            // A token restored from localStorage remains remembered when refresh rotation writes its replacement.
            persistRefreshToken = true
            return restored
        }
        return tabLifetime.read(KEY)?.also { restored -> refreshTokenInMemory = restored }
    }

    override suspend fun writeRefreshToken(token: String) {
        refreshTokenInMemory = token
        if (persistRefreshToken) {
            persistent.write(KEY, token)
            tabLifetime.remove(KEY)
        } else {
            tabLifetime.write(KEY, token)
            persistent.remove(KEY)
        }
    }

    override suspend fun clearRefreshToken() {
        refreshTokenInMemory = null
        persistent.remove(KEY)
        tabLifetime.remove(KEY)
    }

    companion object {
        const val KEY = "chatting.refresh"
    }
}
