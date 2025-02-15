package com.example.acrid.Helper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.acrid.BuildConfig;
import com.example.acrid.Constant.DB;
import com.example.acrid.Model.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ChatRepo {
    private static final FirebaseFirestore mFirebase = FirebaseFirestore.getInstance();
    private static final FirebaseDatabase mFireDB = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_SOCKET_URL);
    private static final long LIMIT_LOAD_MESSAGE= 20;

    public ChatRepo() {

    }

    public static void sendMessage(String conversationID, Message message, Consumer<Boolean> isSuccess ){

        CollectionReference chatRef=mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString());

        String newID = chatRef.document().getId();
        message.setStatus(DB.CHAT_STATUS.SENT.toString());
        message.setMessageUID(newID);
        message.setTimestamp(System.currentTimeMillis());
        chatRef.document(newID)
                .set(message)
                .addOnSuccessListener(unused -> {
                    isSuccess.accept(true);
                    Log.e("sendMessage: ","sent" );
                    updateConversation(conversationID,newID);
                })
                .addOnFailureListener(e -> {
                    Log.e("sendMessage: ",e.getMessage() );
                    message.setStatus(DB.CHAT_STATUS.FAILED.toString());
                    isSuccess.accept(false);
                });

    }

    private static void updateConversation(String conversationsUID,String chatUID){
        Map<String,Object> data = new HashMap<>();
        data.put(DB.CONVERSATIONS_COLLECTION.LAST_UPDATED_AT.toString(),System.currentTimeMillis());
        data.put(DB.CONVERSATIONS_COLLECTION.LAST_MESSAGE.toString(),chatUID);
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .update(data)
                .addOnSuccessListener(unused -> {
                    Log.e("updateConversation: ", "ok");
                })
                .addOnFailureListener(e -> {
                    Log.e( "updateConversation: ",e.getMessage() );
                });
    }

    public static void getMessageList(String conversationsUID, MutableLiveData<List<Message>> data){
        if(data== null) return;
        List<Message> list = new ArrayList<>();
        HashSet<String> existingMessageIds = new HashSet<>();
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString())
                .orderBy(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(), Query.Direction.DESCENDING)
                .limit(LIMIT_LOAD_MESSAGE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                        Message message = documentSnapshot.toObject(Message.class);
                        if(message!=null)list.add(message);
                        existingMessageIds.add(message.getMessageUID());
                    }
                    Collections.reverse(list);
                    data.setValue(list);
                    listenForNewMessage(conversationsUID,list,data,existingMessageIds);
                });
    }

    private static void listenForNewMessage(String conversationsUID, List<Message> list,MutableLiveData<List<Message>> data, HashSet<String> existingMessageIds) {
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString())
                .orderBy(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(),Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if(error!=null){
                        Log.e("listenForNewMessage: ", error.getMessage());
                        return;
                    }
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        data.setValue(new ArrayList<>());
                        return;
                    }
                    boolean hasNewMessage = false;
                    List<Message> newMessages = new ArrayList<>();
                    for(DocumentChange change: queryDocumentSnapshots.getDocumentChanges()){
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message newMessage = change.getDocument().toObject(Message.class);
                            if (!existingMessageIds.contains(newMessage.getMessageUID())) {
                                newMessages.add(newMessage);
                                existingMessageIds.add(newMessage.getMessageUID());
                                hasNewMessage = true;
                            }
                        }
                    }
                    if (hasNewMessage) {
                        list.addAll(newMessages);
                        data.postValue(list);
                    }
                });
    }
    public static MutableLiveData<Boolean> getStatus(String userUID) {
        MutableLiveData<Boolean> data = new MutableLiveData<>(false);
        DatabaseReference userStatusRef = mFireDB.getReference(DB.USER_COLLECTION.NAME.toString())
                .child(userUID)
                .child("status");

        userStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    data.postValue("online".equals(status));
                } else {
                    data.postValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi lấy trạng thái user: " + error.getMessage());
            }
        });

        return data;
    }
    public static void getPreviousMessages(String conversationsUID, long timestamp,Consumer<List<Message>> list) {
        Log.e("getPreviousMessages: ",timestamp+"" );
        List<Message> listmsg = new ArrayList<>();
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString())
                .orderBy(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(), Query.Direction.DESCENDING)
                .startAfter(timestamp)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Message message = documentSnapshot.toObject(Message.class);
                        if (message != null) listmsg.add(message);
                    }
                    Collections.reverse(listmsg);
                    list.accept(listmsg);
                })
                .addOnFailureListener(e -> {
                    Log.e("getPreviousMessages", "Lỗi: " + e.getMessage());
                    list.accept(Collections.emptyList());
                });
    }


}
