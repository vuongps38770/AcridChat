package com.example.acrid.State

sealed class LoginState {
    object Success:LoginState();
    object Loading:LoginState();
    data class Error(val errors: List<LoginError>):LoginState();
}
enum class LoginError {
    EMPTY_EMAIL,
    EMPTY_PASSWORD,
    INVALID_CREDENTIALS,
    UNREGISTERED_EMAIL,
    OTHER_ERROR
}