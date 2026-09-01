package com.hesham.chatting.web

import com.hesham.chatting.web.auth.LoginPageController
import com.hesham.chatting.web.auth.RegisterPageController
import com.hesham.chatting.web.chat.ChatPageController
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun main() {
    val sdk = SdkProvider.create()
    val scope = MainScope()
    window.onload = {
        scope.launch {
            sdk.start()
            val page = document.body?.getAttribute("data-page")
            AppRouter(sdk).guard(page) {
                when (page) {
                    AppRouter.REGISTER -> RegisterPageController(sdk, scope = scope).bind()
                    AppRouter.LOGIN -> LoginPageController(sdk, SdkProvider.tokenStorage, scope = scope).bind()
                    AppRouter.CHAT -> ChatPageController(sdk, scope = scope).bind()
                }
            }
        }
    }
    window.addEventListener("beforeunload", { scope.launch { sdk.shutdown() } })
}
