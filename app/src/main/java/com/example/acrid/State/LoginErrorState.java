package com.example.acrid.State;

public class LoginErrorState{
    public enum LoginState {
        SUCCESS,
        EMPTY_EMAIL,
        EMPTY_PASSWORD,
        INVALID_CREDENTIALS,
        UNREGISTERED_EMAIL,
        OTHER_ERROR
    }

        private LoginState state;
        private String errorMessage;

        public LoginErrorState(LoginState state) {
            this.state = state;
            this.errorMessage = "";
        }

        public LoginErrorState(LoginState state, String errorMessage) {
            this.state = state;
            this.errorMessage = errorMessage;
        }

        public LoginState getState() {
            return state;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

