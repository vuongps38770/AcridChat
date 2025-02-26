package com.example.acrid.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.AuthRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.State.LoginState;

public class LoginViewModel extends ViewModel {
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password= new MutableLiveData<>();
    public MutableLiveData<LoginState> loginState= new MutableLiveData<>();

    public LoginViewModel() {
        email.setValue("daotanquocvuongfacker@gmail.com");
        password.setValue("123456");
        loginState.observeForever(loginState1 -> {
            if(loginState1 instanceof LoginState.Success) {
                initToken();

            }
        });
    }

    public void loginWithEmailAndPW(){
        AuthRepo.loginWithEmailAndPassword(email.getValue(),password.getValue(),loginState);
    }
    public void initToken(){
        UserRepo.getUserUID().observeForever(string -> {
            if(string!=null&&!string.isEmpty()){
                UserRepo.saveToken(string);
            }
        });

    }


}

