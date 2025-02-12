package com.example.acrid.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.ConversationRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Conversation;

import java.util.List;

public class HomeViewModel extends ViewModel {
    public MutableLiveData<String> userUID = new MutableLiveData<>();
    public MutableLiveData<List<Conversation>> conversationListData = new MutableLiveData<>();



    public HomeViewModel() {
        userUID.setValue(UserRepo.getCurrentUserUID());
        initConversationListData();
        conversationListData.observeForever(conversations -> {
            for (Conversation conversation:conversations){
                Log.e("HomeViewModel: ", conversation.getDocumentId()+"///"+conversation.getPartner().getIDName());
            }
        });
    }

    private void initConversationListData(){
        ConversationRepo.getListConversation(UserRepo.getCurrentUserUID()).observeForever(conversations -> {
            conversationListData.setValue(conversations);
        });
    }

}
