package com.hesham.chatting.web

import com.hesham.chatting.shared.ChattingSdk
import com.hesham.chatting.shared.config.SdkConfig
import com.hesham.chatting.web.auth.WebTokenStorage
import com.hesham.chatting.web.config.API_BASE_URL
import com.hesham.chatting.web.config.WS_URL

object SdkProvider {
    val tokenStorage = WebTokenStorage()

    fun create(): ChattingSdk = ChattingSdk(
        config = SdkConfig(apiBaseUrl = API_BASE_URL, wsUrl = WS_URL),
        storage = tokenStorage,
    )
}
