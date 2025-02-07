package com.example.acrid.Helper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.acrid.Constant.DB;
import com.example.acrid.Model.People;
import com.example.acrid.Model.User;
import com.example.acrid.State.LoginError;
import com.example.acrid.State.LoginErrorState;

import com.example.acrid.State.LoginState;
import com.example.acrid.State.SignUpErorr;
import com.example.acrid.State.SignUpState;
import com.example.acrid.State.SimpleCallBack;
import com.example.acrid.View.Activity.SignUp;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class UserRepo {
    //hàm này chưa xong
    public static MutableLiveData<List<String>> getFriendUIDList(String user){
        MutableLiveData<List<String>> data= new MutableLiveData<>();
        List<String> uidList = new ArrayList<>();
        data.setValue(uidList);
        return data;
    };
    private static FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Nullable
    public static String getCurrentUserUID() {
        mAuth.getCurrentUser().getUid();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        } else {
            return null;
        }
    }
    public static MutableLiveData<String> getUserUID() {
        MutableLiveData<String> userUID = new MutableLiveData<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userUID.setValue(currentUser != null ? currentUser.getUid() : null);
        return userUID;
    }
    public static MutableLiveData<List<User>> getUser() {
        MutableLiveData<List<User>> user = new MutableLiveData<>();
        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        for (int i = 0; i < task.getResult().size(); i++) {
                            users.add(task.getResult().toObjects(User.class).get(i));
                        }
                        user.setValue(users);
                    }
                });
        return user;
    }
    public static MutableLiveData<List<People>> getPeopleByEmailorIDName(String emailorIDName) {
        MutableLiveData<List<People>> peopleData = new MutableLiveData<>();
        List<People> list= new ArrayList<>();
        if(emailorIDName.isEmpty()){
            peopleData.setValue(list);
            return peopleData;
        }
        // chia làm 2 query để tìm kiếm theo email và theo IDName

        // query tìm kiếm theo email
        Task<QuerySnapshot> emailQuery = mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .whereEqualTo(DB.USER_COLLECTION.EMAIL.toString(), emailorIDName)
                .get();

        // query tìm kiếm theo IDName
        Task<QuerySnapshot> IDNameQuery = mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .whereGreaterThanOrEqualTo(DB.USER_COLLECTION.ID_NAME.toString(), emailorIDName)
                .whereLessThanOrEqualTo(DB.USER_COLLECTION.ID_NAME.toString(), emailorIDName + "\uf8ff")
                .get();

        // kết hợp 2 query
        Tasks.whenAllSuccess(emailQuery, IDNameQuery)
                .addOnSuccessListener(results -> {
                    Log.e( "getPeopleByEmailorIDName: ", "ok"+results.size()+results.toString());
                    // ctdl set để lọc ra những người trùng nhau (set chỉ chứa phần tử khác nhau)
                    HashSet<String> set = new HashSet<>();
                    for (Object queryDocumentSnapshots : results) {
                        if(queryDocumentSnapshots instanceof QuerySnapshot){
                            QuerySnapshot querySnapshot = (QuerySnapshot) queryDocumentSnapshots;
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                People people = documentSnapshot.toObject(People.class);

                                if (people != null && !set.contains(people.getUserUID())&& !people.getUserUID().equals(UserRepo.getCurrentUserUID())){
                                    set.add(people.getUserUID());
                                    list.add(people);
                                    Log.e("getPeopleByEmailorIDName: ", people.getIDName()+""+people.getEmail());
                                }
                            }
                        }
                    }
                    peopleData.setValue(list);

                })
                .addOnFailureListener(e -> {
                    Log.d("FindPeopleViewModel", "getPeopleByEmailorIDName: " + e.getMessage());
                });
        return peopleData;
    }

    public static void checkFriendShipStatus(String userUID, String otherUID, SimpleCallBack<String> callBack){
        CollectionReference friendListRef = mFirestore.collection("friendlist");
        friendListRef.whereEqualTo("senderID",userUID)
                .whereEqualTo("receiverID",otherUID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots.isEmpty()){
                        callBack.onSucess(DB.FRIEND_STATUS.NONE.toString());
                    }else {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String status = doc.getString("status");
                        callBack.onSucess(status);
                    }
                })
                .addOnFailureListener(e -> {
                    callBack.onError(e.getMessage());
                    Log.e("Firestore", "Lỗi khi lấy trạng thái bạn bè", e);
                });
    }
    public static void addFriend(String senderUID,String receiverUID,SimpleCallBack callBack) {
        DocumentReference pendingRef = mFirestore.collection("friendlist")
                .document(senderUID+"_"+receiverUID);
        Map<String, Object> friend = new HashMap<>();
        friend.put("UID",senderUID+"_"+receiverUID);
        friend.put("status", "pending");
        friend.put("senderID", senderUID);
        friend.put("receiverID", receiverUID);
        friend.put("timestamp", System.currentTimeMillis());
        pendingRef.set( friend).addOnSuccessListener(aVoid -> {
            callBack.onSucess(null);
        }).addOnFailureListener(e -> {
            callBack.onError(e.getMessage());
            Log.d("UserRepo", "addFriend: " + e.getMessage());
        });
    }

    public static void deleteFriend(String friendUID) {
        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(mAuth.getCurrentUser().getUid())
                .collection(DB.FRIEND_LIST_COLLECTION.NAME.toString())
                .document(friendUID)
                .delete();
    }

}