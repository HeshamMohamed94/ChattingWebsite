package com.hesham.chatting.web

import com.hesham.chatting.web.dom.ChatIds
import com.hesham.chatting.web.dom.LoginIds
import com.hesham.chatting.web.dom.RegisterIds
import com.hesham.chatting.web.test.ShippedHtml
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdsExistTest {
    @Test
    fun registerIdsExistInShippedIndexHtml() = assertIds(ShippedHtml.REGISTER, RegisterIds.all)

    @Test
    fun loginIdsExistInShippedLoginHtml() = assertIds(ShippedHtml.LOGIN, LoginIds.all)

    @Test
    fun shippedAuthPagesUseSignInTerminology() {
        val authHtml = ShippedHtml.REGISTER + ShippedHtml.LOGIN
        assertTrue(authHtml.contains(">Sign in<"))
        assertFalse(Regex(">\\s*(?:Login|Log in)\\s*<", RegexOption.IGNORE_CASE).containsMatchIn(authHtml))
    }

    @Test
    fun chatIdsExistInShippedChatHtml() = assertIds(ShippedHtml.CHAT, ChatIds.all)

    private fun assertIds(html: String, ids: List<String>) {
        ids.forEach { id ->
            val declaration = Regex("""\bid\s*=\s*["']${Regex.escape(id)}["']""")
            assertTrue(declaration.containsMatchIn(html), "Shipped HTML is missing id=\"$id\"")
        }
    }
}
