package com.example.acrid.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.UserRepo;

public class HomeViewModel extends ViewModel {
    public MutableLiveData<String> userUID = new MutableLiveData<>();

    public HomeViewModel() {
        userUID.setValue(UserRepo.getCurrentUserUID());
    }

}
