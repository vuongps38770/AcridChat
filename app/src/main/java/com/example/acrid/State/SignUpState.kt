package com.example.acrid.State

sealed class SignUpState {
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val error: List<SignUpErorr>) : SignUpState()
}

enum class SignUpErorr {
    EMAIL_ALREADY_EXISTS,
    EMAIL_EMPTY,
    PASSWORD_EMPTY,
    NICKNAME_EMPTY,
    RE_PASSWORD_EMPTY,
    EMAIL_INVALID,
    PASSWORD_INVALID,
    PASSWORD_NOT_MATCH,
    OTHER
}