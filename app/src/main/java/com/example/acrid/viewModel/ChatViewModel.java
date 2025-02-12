package com.example.acrid.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Model.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    public MutableLiveData<List<Chat>> chatData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<String> chatUID = new MutableLiveData<>("");

    public ChatViewModel() {

    }


}
