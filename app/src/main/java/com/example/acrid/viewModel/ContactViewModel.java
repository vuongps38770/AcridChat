package com.example.acrid.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.ConversationRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.People;
import com.example.acrid.State.SimpleCallBack;
import com.example.acrid.View.Dialog.InfoDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactViewModel extends ViewModel {
    public MutableLiveData<List<Friend>> friendListData= new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Friend> selectedFriend= new MutableLiveData<>();
    private List<Friend> ogFriendList = new ArrayList<>(friendListData.getValue());
    public MutableLiveData<List<People>> ACPList= new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<String> searchFriendQuerry = new MutableLiveData<>("");
    public MutableLiveData<Boolean> isOnError= new MutableLiveData<>(false);
    public MutableLiveData<String> conversationID = new MutableLiveData<>("");


    public ContactViewModel(){
        getACPlist();
        registerUpdateSearchData();
        reisterFriendData();
    }
    public void search(String qerry){
        searchFriendQuerry.postValue(qerry);
    }

    private void getACPlist(){
        UserRepo.getACPFriendList(UserRepo.getCurrentUserUID()).observeForever( newData -> {
            Log.e("getACPlist: ", newData.size()+"");
            ACPList.postValue(newData);
        });
    }

    private void reisterFriendData(){
        UserRepo.getFriendList(UserRepo.getCurrentUserUID()).observeForever(friends -> {
            friends.sort(Comparator.comparing(Friend::getDisplayName));
            ogFriendList.clear();
            ogFriendList.addAll(friends);
            friendListData.postValue(friends);
        });
    }
    public void gotoChat(String partnerID){
        ConversationRepo.checkIsExistConversation(UserRepo.getCurrentUserUID(), partnerID, new ConversationRepo.CheckIsExistConversationCallBack() {
            @Override
            public void onExistConversation(String conversationUID) {
                conversationID.postValue(conversationUID);
            }
            @Override
            public void onUnExistConversation() {
                ConversationRepo.createConversation(UserRepo.getCurrentUserUID(), partnerID, new SimpleCallBack<String>() {
                    @Override
                    public void onSucess(@Nullable String data) {
                        conversationID.postValue(data);
                    }
                    @Override
                    public void onError(String message) {
                        isOnError.postValue(true);
                    }
                });
            }

            @Override
            public void onError() {
                isOnError.postValue(true);
            }
        });
    }
    public void acpFriend(People person ){

        UserRepo.acceptFriend(person.getUserUID(), UserRepo.getCurrentUserUID(), new SimpleCallBack() {
            @Override
            public void onSucess(@Nullable Object data) {
                Log.e( "onSucess: ", "ok");
                reisterFriendData();
                List<People> currentList = ACPList.getValue();
                if (currentList != null) {
                    currentList.remove(person);
                    ACPList.postValue(currentList);
                }
            }

            @Override
            public void onError(String message) {
                Log.e("onError: ",message );
            }
        });
    }
    private void registerUpdateSearchData(){
        searchFriendQuerry.observeForever(querry -> {
            if(querry.isEmpty()){
                friendListData.postValue(ogFriendList);
                return;
            }
            List<Friend> temp = new ArrayList<>();
            for(Friend friend:ogFriendList){
                if (friend.getIDName().toLowerCase().contains(querry.toLowerCase())){
                    temp.add(friend);
                }
            }
            friendListData.postValue(temp);
        });
    }

    public void showProfile(String friendUID,Context context) {
        InfoDialog dialog = new InfoDialog(friendUID,context);
        dialog.show();
    }
}
