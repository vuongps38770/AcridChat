package com.example.acrid.Helper;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.acrid.Constant.DB;
import com.example.acrid.Model.Conversation;
import com.example.acrid.Model.Friend;
import com.example.acrid.State.SimpleCallBack;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ConversationRepo {

    private static FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    public static void checkIsExistConversation(String user1ID, String user2ID, CheckIsExistConversationCallBack callBack){
        mFirestore.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .whereArrayContains(DB.CONVERSATIONS_COLLECTION.PARTICIPANT_IDS.toString(),user1ID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean conversationExists = false;
                    String existingConversationId = null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        List<String> ids = (List<String>) doc.get(DB.CONVERSATIONS_COLLECTION.PARTICIPANT_IDS.toString());
                        if (ids != null && ids.contains(user2ID)) {
                            conversationExists = true;
                            existingConversationId = doc.getId();
                            break;
                        }
                    }
                    if (conversationExists) {
                        callBack.onExistConversation(existingConversationId);
                    } else {
                        callBack.onUnExistConversation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("checkIsExistConversation: ",e.getMessage() );
                    callBack.onError();
                });
    }

    public static void createConversation(String user1ID, String user2ID, SimpleCallBack<String> callBack){
        CollectionReference conversationsRef = mFirestore.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString());

        String newID = conversationsRef.document().getId();
        List<String> ids = Arrays.asList(user1ID,user2ID);
        Collections.sort(ids);

        Map<String, Object> data = new HashMap<>();
        data.put(DB.CONVERSATIONS_COLLECTION.DOCUMMENT_ID.toString(),newID);
        data.put(DB.CONVERSATIONS_COLLECTION.INITIATED_AT.toString(),System.currentTimeMillis());
        data.put(DB.CONVERSATIONS_COLLECTION.INITIATEDBY.toString(),user1ID);
        data.put(DB.CONVERSATIONS_COLLECTION.LAST_MESSAGE.toString(),new HashMap<>());
        data.put(DB.CONVERSATIONS_COLLECTION.LAST_UPDATED_AT.toString(),System.currentTimeMillis());
        data.put(DB.CONVERSATIONS_COLLECTION.PARTICIPANT_IDS.toString(),ids);
        conversationsRef.document(newID).set(data)
                .addOnSuccessListener(unused -> callBack.onSucess(newID))
                .addOnFailureListener(e -> {
                        callBack.onError(e.getMessage());
                        Log.e("createConversation: ", e.getMessage());
                    }
                );
    }

    public static MutableLiveData<List<Conversation>> getListConversation(String userUID) {
        MutableLiveData<List<Conversation>> data = new MutableLiveData<>();

        mFirestore.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .whereArrayContains(DB.CONVERSATIONS_COLLECTION.PARTICIPANT_IDS.toString(), userUID)
                .orderBy(DB.CONVERSATIONS_COLLECTION.LAST_UPDATED_AT.toString(), Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("getListConversation", "Lỗi lấy danh sách hội thoại: " + error.getMessage());
                        data.setValue(new ArrayList<>());
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        data.setValue(new ArrayList<>());
                        return;
                    }

                    List<Conversation> list = new ArrayList<>();


                    AtomicInteger pendingTasks = new AtomicInteger(queryDocumentSnapshots.size());

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Conversation conversation = doc.toObject(Conversation.class);
                        if (conversation == null) {
                            pendingTasks.decrementAndGet();
                            continue;
                        }
                        Log.e("getListConversation: ", conversation.getDocumentId() + "/" + conversation.getLastUpdatedAt());

                        String friendUID = getFriendUID(conversation.getParticipantIds(), userUID);
                        if (friendUID == null) {
                            pendingTasks.decrementAndGet();
                            continue;
                        }

                        list.add(conversation);

                        UserRepo.getPeopleByUID(friendUID, Friend.class, friend -> {
                            if (friend != null) {
                                conversation.setPartner(friend);
                            }

                            // Khi một truy vấn hoàn thành, giảm số đếm
                            if (pendingTasks.decrementAndGet() == 0) {
                                // Khi tất cả các truy vấn hoàn thành, cập nhật LiveData
                                data.setValue(list);
                            }
                        });
                    }
                });

        return data;
    }

    private static String getFriendUID(List<String> participantIds, String userUID) {
        for (String id : participantIds) {
            if (!id.equals(userUID)) {
                Log.d("getListConversationIdPartner", "Tìm thấy bạn chat: " + id);
                return id;
            }
        }
        return null;
    }
    public interface CheckIsExistConversationCallBack{
        void onExistConversation(String conversationID);
        void onUnExistConversation();
        void onError();
    }


}
