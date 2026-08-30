package com.hesham.chatting.web

import kotlinx.browser.document

fun main() {
    val message = "ChattingWebsite Kotlin/JS is running."
    println(message)
    document.getElementById("app")?.textContent = message
}

