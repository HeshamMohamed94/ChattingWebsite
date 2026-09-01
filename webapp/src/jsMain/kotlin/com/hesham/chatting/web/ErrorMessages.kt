package com.hesham.chatting.web

import com.hesham.chatting.shared.network.UiError

private val fieldMessages = mapOf(
    "FIRST_NAME_REQUIRED" to "First name is required.",
    "FIRST_NAME_TOO_LONG" to "First name must be 50 characters or fewer.",
    "FIRST_NAME_MUST_CONTAIN_LETTER" to "First name must contain a letter.",
    "LAST_NAME_REQUIRED" to "Last name is required.",
    "LAST_NAME_TOO_LONG" to "Last name must be 50 characters or fewer.",
    "LAST_NAME_MUST_CONTAIN_LETTER" to "Last name must contain a letter.",
    "USERNAME_TOO_SHORT" to "Username must be at least 3 characters.",
    "USERNAME_TOO_LONG" to "Username must be 30 characters or fewer.",
    "USERNAME_MUST_START_WITH_LETTER" to "Username must start with a letter.",
    "USERNAME_INVALID_CHARACTERS" to "Use only lowercase letters, numbers, underscores, or dots.",
    "USERNAME_CONSECUTIVE_DOTS" to "Username cannot contain consecutive dots.",
    "EMAIL_REQUIRED" to "Email is required.",
    "EMAIL_TOO_LONG" to "Email is too long.",
    "EMAIL_INVALID" to "Enter a valid email address.",
    "PASSWORD_TOO_SHORT" to "Password must be at least 10 bytes.",
    "PASSWORD_TOO_LONG" to "Password must be 72 bytes or fewer.",
    "PASSWORD_MUST_CONTAIN_LETTER" to "Password must contain a letter.",
    "PASSWORD_MUST_CONTAIN_DIGIT" to "Password must contain a number.",
    "PASSWORD_MATCHES_USERNAME" to "Password cannot match your username.",
    "PASSWORD_MATCHES_EMAIL" to "Password cannot match your email name.",
    "CONFIRM_PASSWORD_MISMATCH" to "Passwords do not match.",
    "TERMS_REQUIRED" to "You must agree to the Terms & Conditions to continue.",
)

fun fieldErrorMessage(code: String): String = fieldMessages[code]
    ?: code.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() }

fun uiErrorMessage(error: UiError?): String? = when (error) {
    null -> null
    UiError.Offline -> "You're offline. Check your connection and try again."
    is UiError.Fields -> error.message
    is UiError.Message -> error.text
}
