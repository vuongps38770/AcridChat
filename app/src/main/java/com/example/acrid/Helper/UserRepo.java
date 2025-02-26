package com.example.acrid.Helper;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.acrid.Constant.DB;
import com.example.acrid.Model.BasePeople;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.People;
import com.example.acrid.Model.User;

import com.example.acrid.State.SimpleCallBack;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserRepo {
    //hàm này chưa xong
    public static MutableLiveData<List<String>> getFriendUIDList(String user){
        MutableLiveData<List<String>> data= new MutableLiveData<>();
        List<String> uidList = new ArrayList<>();
        data.setValue(uidList);
        return data;
    };
    public static MutableLiveData<List<Friend>> getFriendList(String userUID){
        MutableLiveData<List<Friend>> data= new MutableLiveData<>();
        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(userUID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        List<String> uidList = (List<String>) documentSnapshot.get(DB.USER_COLLECTION.FRIEND_UID_LIST.toString());
                        if(uidList!=null&&!uidList.isEmpty()){
                            getBasePeopleByUID(uidList,data, Friend.class);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    data.postValue(new ArrayList<>());
                    Log.e("getFriendList: ", e.getMessage());
                });
        return data;
    };
    public static MutableLiveData<List<People>> getACPFriendList(String ownerUserUID)  {
        MutableLiveData<List<People>> data= new MutableLiveData<>();
        mFirestore.collection(DB.FRIEND_LIST_COLLECTION.NAME.toString())
                .whereEqualTo(DB.FRIEND_LIST_COLLECTION.RECEIVER_ID.toString(),ownerUserUID)
                .whereEqualTo(DB.FRIEND_LIST_COLLECTION.STATUS.toString(),DB.FRIEND_STATUS.PENDING.toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots.isEmpty()){
                        data.postValue(new ArrayList<>());
                        Log.e( "getACPFriendList: ","empty" );
                        return;
                    }
                    List<String> uidList = new ArrayList<>();
                    for(DocumentSnapshot doc: queryDocumentSnapshots ){
                        String senderID = doc.getString(DB.FRIEND_LIST_COLLECTION.SENDER_ID.toString());
                        if(senderID==null||senderID.isEmpty()){
                            continue;
                        }
                        uidList.add(senderID);
                    }
                    getBasePeopleByUID(uidList,data,People.class);
                })
                .addOnFailureListener(e -> {
                    data.postValue(new ArrayList<>());
                    Log.e("getFriendList: ", Objects.requireNonNull(e.getMessage()));
                });
        return data;
    }
    ///lấy data của lớp thuộc lớp base people
    private static <T extends BasePeople> void getBasePeopleByUID(List<String> UIDList, MutableLiveData<List<T>> data, Class<T> type){
        List<T> friendList = new ArrayList<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String uid : UIDList) {
            Task<DocumentSnapshot> task = mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        T friend = documentSnapshot.toObject(type);
                        if (friend != null) friendList.add(friend);
                    });
            tasks.add(task);
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(result -> {
            data.postValue(friendList);
        });
    }
    private static FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Nullable
    public static String getCurrentUserUID() {
        if(mAuth.getCurrentUser()==null)
            return null;
        mAuth.getCurrentUser().getUid();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        } else {
            return null;
        }
    }
    public static void acceptFriend(String senderUID ,String userUID, SimpleCallBack callBack){
        WriteBatch batch = mFirestore.batch();
        DocumentReference friendRequestRef = mFirestore.collection(DB.FRIEND_LIST_COLLECTION.NAME.toString())
                .document(senderUID + "_" + userUID);

        DocumentReference senderRef = mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(senderUID);

        DocumentReference receiverRef = mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(userUID);

        mFirestore.runTransaction(transaction -> {
            // Cập nhật trạng thái lời mời thành "ACCEPTED"
            transaction.update(friendRequestRef, DB.FRIEND_LIST_COLLECTION.STATUS.toString(), DB.FRIEND_STATUS.ACCEPTED.toString());

            // Thêm UID vào danh sách bạn bè của cả sender và receiver
            transaction.update(senderRef, DB.USER_COLLECTION.FRIEND_UID_LIST.toString(), FieldValue.arrayUnion(userUID));
            transaction.update(receiverRef, DB.USER_COLLECTION.FRIEND_UID_LIST.toString(), FieldValue.arrayUnion(senderUID));
            return null;
        }).addOnSuccessListener(aVoid -> {
            callBack.onSucess(null);
        }).addOnFailureListener(e -> {
            callBack.onError(e.getMessage());
        });
    }
    public static MutableLiveData<String> getUserUID() {
        MutableLiveData<String> userUID = new MutableLiveData<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userUID.setValue(currentUser != null ? currentUser.getUid() : null);
        return userUID;
    }


    public static MutableLiveData<User> getUserByUID(String userUID) {
        MutableLiveData<User> data = new MutableLiveData<>();
        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(userUID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = (User) documentSnapshot.toObject(User.class);
                    if(user!=null) data.postValue(user);
                    else data.postValue(null);
                })
                .addOnFailureListener(e -> {
                    data.postValue(null);
                });

        return data;
    }
    public static <T extends BasePeople> void getPeopleByUID(String UID,Class<T> type, PeopleCallBack<T> callBack){
        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(UID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    T person = documentSnapshot.toObject(type);
                    callBack.call(person);
                })
                .addOnFailureListener(e -> {
                    Log.e("getPeopleByUID: ", e.getMessage());
                    callBack.call(null);
                });
    }
    public interface PeopleCallBack<T extends BasePeople>{
        void call(T person);
    }
    public static <T extends BasePeople> MutableLiveData<List<T>> getPeopleByEmailorIDName(String emailorIDName,Class<T> type) {
        MutableLiveData<List<T>> peopleData = new MutableLiveData<>();
        List<T> list= new ArrayList<>();
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
                                T people = documentSnapshot.toObject(type);
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

    public static void saveToken(String userUID) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("saveToken", "Lỗi lấy token: " + Objects.requireNonNull(task.getException()).getMessage());
                        return;
                    }

                    String newToken = task.getResult();
                    if (newToken == null || newToken.isEmpty()) {
                        Log.e("saveToken", "Token trống, không lưu.");
                        return;
                    }

                    // Kiểm tra token cũ để tránh cập nhật không cần thiết
                    mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                            .document(userUID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String oldToken = documentSnapshot.getString(DB.USER_COLLECTION.TOKEN.toString());
                                    if (newToken.equals(oldToken)) {
                                        Log.e("saveToken", "Token không thay đổi, không cập nhật.");
                                        return;
                                    }
                                }

                                // Nếu token thay đổi hoặc chưa có, lưu mới
                                mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                                        .document(userUID)
                                        .update(DB.USER_COLLECTION.TOKEN.toString(), newToken)
                                        .addOnSuccessListener(unused -> Log.e("saveToken", "Token mới đã được lưu."))
                                        .addOnFailureListener(e -> Log.e("saveToken", "Lỗi khi lưu token: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> Log.e("saveToken", "Lỗi khi lấy token cũ: " + e.getMessage()));
                });
    }

    public static void clearToken(String userUID){
        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                .document(userUID)
                .set(Collections.singletonMap(DB.USER_COLLECTION.TOKEN.toString(), ""), SetOptions.merge())
                .addOnSuccessListener(runnable -> {
                    Log.e("saveToken: ", "dã xoa");
                })
                .addOnFailureListener(e -> {
                    Log.e( "saveToken: ",e.getMessage() );
                });

    }

}