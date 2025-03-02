package com.example.acrid.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.ConversationRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Conversation;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    public MutableLiveData<String> userUID = new MutableLiveData<>();
    public MutableLiveData<List<Conversation>> conversationListData = new MutableLiveData<>();
    public MutableLiveData<List<Conversation>> ogConversationListData = new MutableLiveData<>();
    public MutableLiveData<String> searchQuery = new MutableLiveData<>("");



    public HomeViewModel() {
        userUID.setValue(UserRepo.getCurrentUserUID());
        initConversationListData();
    }

//    private void search(String query) {
//        if (conversationListData==null) return;
//        if (query.isEmpty()) {
//            conversationListData.setValue(ogConversationListData.getValue());
//            return;
//        }
//
//        List<Conversation> filteredList = new ArrayList<>();
//        String lowerQuery = query.toLowerCase();
//
//        for (Conversation conversation : ogConversationListData.getValue()) {
//            String idName = conversation.getPartner().getIDName().toLowerCase();
//            String displayName = conversation.getPartner().getDisplayName().toLowerCase();
//
//            if (idName.equals(lowerQuery) || isSubsequence(lowerQuery, displayName)) {
//                filteredList.add(conversation);
//            }
//        }
//        conversationListData.setValue(filteredList);
//
//    }

    private void initConversationListData(){
        ConversationRepo.getListConversation(UserRepo.getCurrentUserUID()).observeForever(conversations -> {
            conversationListData.setValue(conversations);
            ogConversationListData.setValue(conversations);
        });
    }



}
