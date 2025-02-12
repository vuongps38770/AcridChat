package com.example.acrid.viewModel;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.People;
import com.example.acrid.State.SimpleCallBack;

import java.util.ArrayList;
import java.util.List;

public class FindPeopleViewModel extends ViewModel {
    public MutableLiveData<String> search = new MutableLiveData<>();
    public MutableLiveData<List<People>> peopleList = new MutableLiveData<>();
    public MutableLiveData<Boolean> isSearching = new MutableLiveData<>();
    public MutableLiveData<Integer> changePos = new MutableLiveData<>();
    public MutableLiveData<String> tempERR = new MutableLiveData<>("");

    public FindPeopleViewModel() {
        isSearching.setValue(false);
        search.setValue("");
        changePos.setValue(-1);
        search.observeForever(query -> {
            if (query != null && !query.trim().isEmpty()&& !query.isEmpty()) {
                searchPeople();
            }else {
                peopleList.postValue(new ArrayList<>());
            }
        });
    }
    public void searchPeople() {
        isSearching.setValue(true);
        UserRepo.getPeopleByEmailorIDName(search.getValue(),People.class).observeForever(people -> {
            peopleList.setValue(people);
            for (People person : people) {
                Log.e( "onSucess: ", person.getEmail());
                UserRepo.checkFriendShipStatus(UserRepo.getCurrentUserUID(), person.getUserUID(), new SimpleCallBack<String>() {
                    @Override
                    public void onSucess(@Nullable String data) {
                        person.setFriendStatus(data);
                        peopleList.setValue(people);
                        Log.e( "onSucess: ", data);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e( "onError: ",message );
                    }
                });

            }
            isSearching.setValue(false);
        });
    }


    public void addFriendClick(){
        Log.e("addFriendClick: ","click" );
        if(changePos.getValue()==-1||changePos.getValue()+1>peopleList.getValue().size()){
            Log.e("addFriendClick: ","not" );
            return;
        }
        People people =peopleList.getValue().get(changePos.getValue());
        String status=people.getFriendStatus();
        if(status.equals(DB.FRIEND_STATUS.NONE.toString())){
            UserRepo.addFriend(UserRepo.getCurrentUserUID(), people.getUserUID(), new SimpleCallBack() {
                @Override
                public void onSucess(@Nullable Object data) {
                    Log.e("onSucess: ","ok" );
                    people.setFriendStatus(DB.FRIEND_STATUS.PENDING.toString());
                    peopleList.postValue(peopleList.getValue());
                }

                @Override
                public void onError(String message) {
                    tempERR.setValue("Có lỗi xảy ra");
                    Log.e("onError: ", message);
                }
            });
        }
    }
    public void addFriend( String friendUID, SimpleCallBack callBack){
        UserRepo.addFriend(UserRepo.getCurrentUserUID(), friendUID,callBack);
    }





}
