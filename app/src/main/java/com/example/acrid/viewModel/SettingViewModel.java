package com.example.acrid.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.AuthRepo;
import com.example.acrid.Helper.UserRepo;

public class SettingViewModel extends ViewModel {
    public MutableLiveData<String> avtUrl = new MutableLiveData<>("");
    public MutableLiveData<String> status = new MutableLiveData<>("");
    public MutableLiveData<String> name = new MutableLiveData<>("");
    public MutableLiveData<String> tempIMGurl = new MutableLiveData<>("");
    public MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<>(false);

    public SettingViewModel() {
        UserRepo.getUserByUID(UserRepo.getCurrentUserUID()).observeForever(user -> {
            if(user!=null){
                avtUrl.postValue(user.getProfileIMG());
                status.postValue(user.getDescription().isEmpty()?"Không có mô tả":user.getDescription());
                name.postValue(user.getIDName());
            }
        });
    }

    public void logout(){
        AuthRepo.logout();
        isLoggedOut.postValue(true);
    }
}
