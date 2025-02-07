package com.example.acrid.viewModel;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthViewModel extends BaseObservable {
    @Bindable
    private MutableLiveData<String> email = new MutableLiveData<>();
    @Bindable
    private MutableLiveData<String> password = new MutableLiveData<>();
    @Bindable
    private MutableLiveData<String> nickname = new MutableLiveData<>();
    @Bindable
    private MutableLiveData<Boolean> signUpSuccess = new MutableLiveData<>();
    @Bindable
    private MutableLiveData<String> signUpError = new MutableLiveData<>();


    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getPassword() {
        return password;
    }

    public LiveData<String> getNickname() {
        return nickname;
    }

    public LiveData<Boolean> getSignUpSuccess() {
        return signUpSuccess;
    }

    public LiveData<String> getSignUpError() {
        return signUpError;
    }

    public void signUp(String email, String password, String nickname) {
        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            signUpError.setValue("empty err");
            return;
        }

    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public void setNickname(String nickname) {
        this.nickname.setValue(nickname);
    }
}

