package com.hesham.chatting.web

import com.hesham.chatting.web.auth.RefreshTokenPersistence
import com.hesham.chatting.web.auth.WebTokenStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WebTokenStorageTest {
    @Test
    fun rememberMeChoosesPersistentOrTabMemoryStorage() = runTest {
        val persistence = FakePersistence()
        val tabLifetime = FakePersistence()
        val storage = WebTokenStorage(persistence, tabLifetime)

        storage.setRememberMe(false)
        storage.writeRefreshToken("tab-only")
        assertNull(persistence.values[WebTokenStorage.KEY])
        assertEquals("tab-only", tabLifetime.values[WebTokenStorage.KEY])
        assertEquals("tab-only", storage.readRefreshToken())

        storage.setRememberMe(true)
        storage.writeRefreshToken("remembered")
        assertEquals("remembered", persistence.values[WebTokenStorage.KEY])
        assertNull(tabLifetime.values[WebTokenStorage.KEY])

        storage.clearRefreshToken()
        assertNull(storage.readRefreshToken())
        assertNull(persistence.values[WebTokenStorage.KEY])
        assertNull(tabLifetime.values[WebTokenStorage.KEY])
    }

    @Test
    fun restoredPersistentTokenKeepsRefreshRotationPersistent() = runTest {
        val persistence = FakePersistence().apply { values[WebTokenStorage.KEY] = "persisted" }
        val storage = WebTokenStorage(persistence, FakePersistence())

        assertEquals("persisted", storage.readRefreshToken())
        storage.writeRefreshToken("rotated")

        assertEquals("rotated", persistence.values[WebTokenStorage.KEY])
    }

    @Test
    fun tabOnlyTokenSurvivesAFreshPageLoadWithoutRememberMe() = runTest {
        // Every navigation between register/login/chat.html tears down all in-memory
        // state and constructs a brand-new WebTokenStorage - this reproduces exactly
        // that page-reload boundary using shared, page-independent storage instances.
        val persistence = FakePersistence()
        val tabLifetime = FakePersistence()

        val registerPageStorage = WebTokenStorage(persistence, tabLifetime)
        registerPageStorage.setRememberMe(false)
        registerPageStorage.writeRefreshToken("session-token")

        val chatPageStorage = WebTokenStorage(persistence, tabLifetime)
        assertEquals("session-token", chatPageStorage.readRefreshToken())
    }
}

private class FakePersistence : RefreshTokenPersistence {
    val values = mutableMapOf<String, String>()
    override fun read(key: String): String? = values[key]
    override fun write(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
}
