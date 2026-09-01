package com.hesham.chatting.web.dom

object RegisterIds {
    const val FORM = "registerForm"
    const val FIRST_NAME = "firstName"
    const val FIRST_NAME_ERROR = "firstNameError"
    const val LAST_NAME = "lastName"
    const val LAST_NAME_ERROR = "lastNameError"
    const val USERNAME = "username"
    const val USERNAME_ERROR = "usernameError"
    const val EMAIL = "email"
    const val EMAIL_ERROR = "emailError"
    const val PASSWORD = "password"
    const val PASSWORD_ERROR = "passwordError"
    const val TOGGLE_PASSWORD = "togglePassword"
    const val CONFIRM_PASSWORD = "confirmPassword"
    const val CONFIRM_PASSWORD_ERROR = "confirmPasswordError"
    const val TOGGLE_CONFIRM = "toggleConfirm"
    const val TERMS = "terms"
    const val TERMS_LINK = "termsLink"
    const val TERMS_ERROR = "termsError"
    const val SUBMIT = "submitBtn"
    const val LOGIN_LINK = "loginLink"
    const val SUCCESS = "successMessage"

    val all = listOf(
        FORM, FIRST_NAME, FIRST_NAME_ERROR, LAST_NAME, LAST_NAME_ERROR, USERNAME, USERNAME_ERROR,
        EMAIL, EMAIL_ERROR, PASSWORD, PASSWORD_ERROR, TOGGLE_PASSWORD, CONFIRM_PASSWORD,
        CONFIRM_PASSWORD_ERROR, TOGGLE_CONFIRM, TERMS, TERMS_LINK, TERMS_ERROR, SUBMIT, LOGIN_LINK, SUCCESS,
    )
}

object LoginIds {
    const val FORM = "loginForm"
    const val USERNAME = "username"
    const val USERNAME_ERROR = "usernameError"
    const val PASSWORD = "password"
    const val PASSWORD_ERROR = "passwordError"
    const val REMEMBER_ME = "rememberMe"
    const val FORGOT_PASSWORD = "forgotPassword"
    const val ERROR = "loginError"
    const val SUBMIT = "loginSubmit"
    const val REGISTER_LINK = "registerLink"

    val all = listOf(
        FORM, USERNAME, USERNAME_ERROR, PASSWORD, PASSWORD_ERROR, REMEMBER_ME,
        FORGOT_PASSWORD, ERROR, SUBMIT, REGISTER_LINK,
    )
}

object ChatIds {
    const val SHELL = "chatShell"
    const val NEW_CHAT_BUTTON = "newChatButton"
    const val LOGOUT_BUTTON = "logoutButton"
    const val CONNECTION_STATUS = "connectionStatus"
    const val CONVERSATION_LOADING = "conversationLoading"
    const val CONVERSATION_ERROR = "conversationError"
    const val CONVERSATION_LIST = "conversationList"
    const val NO_CONVERSATIONS = "noConversations"
    const val NO_CONVERSATION_SELECTED = "noConversationSelected"
    const val THREAD = "thread"
    const val BACK_TO_CONVERSATIONS = "backToConversations"
    const val THREAD_TITLE = "threadTitle"
    const val THREAD_SUBTITLE = "threadSubtitle"
    const val THREAD_ERROR = "threadError"
    const val LOAD_MORE = "loadMoreButton"
    const val MESSAGE_LIST = "messageList"
    const val NO_MESSAGES = "noMessages"
    const val COMPOSER_FORM = "composerForm"
    const val COMPOSER_INPUT = "composerInput"
    const val SEND = "sendButton"
    const val NEW_CHAT_PANE = "newChatPane"
    const val CLOSE_NEW_CHAT = "closeNewChat"
    const val USER_SEARCH_INPUT = "userSearchInput"
    const val USER_SEARCH_STATUS = "userSearchStatus"
    const val NEW_CHAT_ERROR = "newChatError"
    const val USER_SEARCH_RESULTS = "userSearchResults"
    const val CONVERSATION_ROW_TEMPLATE = "conversationRowTemplate"
    const val MESSAGE_BUBBLE_TEMPLATE = "messageBubbleTemplate"

    val all = listOf(
        SHELL, NEW_CHAT_BUTTON, LOGOUT_BUTTON, CONNECTION_STATUS, CONVERSATION_LOADING,
        CONVERSATION_ERROR, CONVERSATION_LIST, NO_CONVERSATIONS, NO_CONVERSATION_SELECTED,
        THREAD, BACK_TO_CONVERSATIONS, THREAD_TITLE, THREAD_SUBTITLE, THREAD_ERROR, LOAD_MORE,
        MESSAGE_LIST, NO_MESSAGES, COMPOSER_FORM, COMPOSER_INPUT, SEND, NEW_CHAT_PANE,
        CLOSE_NEW_CHAT, USER_SEARCH_INPUT, USER_SEARCH_STATUS, NEW_CHAT_ERROR, USER_SEARCH_RESULTS,
        CONVERSATION_ROW_TEMPLATE, MESSAGE_BUBBLE_TEMPLATE,
    )
}
