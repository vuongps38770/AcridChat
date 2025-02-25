package com.example.acrid.viewModel;


import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.AuthRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.User;
import com.example.acrid.State.SignUpErorr;
import com.example.acrid.State.SignUpState;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SignUpViewModel extends ViewModel {
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> rePassword = new MutableLiveData<>();
    public MutableLiveData<String> nickName = new MutableLiveData<>();
    public MutableLiveData<SignUpState> signUpState = new MutableLiveData<>();

    public SignUpViewModel() {
        email.setValue("");
        password.setValue("");
        rePassword.setValue("");
        nickName.setValue("");

    }

    public void signUp() {
        Log.e("signUp: ",email.getValue()+"/"+password.getValue()+"/"+nickName.getValue() );

        List<SignUpErorr> errors = new ArrayList<>();
        if(email.getValue().isEmpty()){
            errors.add(SignUpErorr.EMAIL_EMPTY);
        }
        if(password.getValue().isEmpty()){
            errors.add(SignUpErorr.PASSWORD_EMPTY);
        }
        if(rePassword.getValue().isEmpty()){
            errors.add(SignUpErorr.RE_PASSWORD_EMPTY);
        }
        if(nickName.getValue().isEmpty()){
            errors.add(SignUpErorr.NICKNAME_EMPTY);
        }
        if(!password.getValue().equals(rePassword.getValue())){
            errors.add(SignUpErorr.PASSWORD_NOT_MATCH);
        }
        if(!errors.isEmpty()){
            signUpState.postValue(new SignUpState.Error(errors));
            Log.e("DEBUG", "Lỗi: " + errors.toString());
            Log.e("DEBUG", "Email hiện tại: " + email.getValue());
            Log.e("DEBUG", "RePassword hiện tại: " + rePassword.getValue());
            return;
        }
        Log.e("signUp: ",email.getValue()+"/"+password.getValue()+"/"+nickName.getValue() );
        User user = new User();
        user.setIDName(nickName.getValue());
        user.setEmail(email.getValue());
        AuthRepo.saveUser(user,password.getValue(),signUpState);
    }
    
}
