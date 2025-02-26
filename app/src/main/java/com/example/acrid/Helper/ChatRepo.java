package com.example.acrid.Helper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.acrid.BuildConfig;
import com.example.acrid.Constant.DB;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.Message;
import com.example.acrid.Model.NotificationBody;
import com.example.acrid.utils.TimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepo {
    private static final FirebaseFirestore mFirebase = FirebaseFirestore.getInstance();
    private static final FirebaseDatabase mFireDB = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_SOCKET_URL);
    private static final long LIMIT_LOAD_MESSAGE= 40;
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(ChatRepo.class);

    public ChatRepo() {

    }

    public static void sendMessage(String conversationID, Message message, String friendToken, Consumer<Boolean> isSuccess ){
        if(message.getMessage().trim().isEmpty()){
            return;
        }
        CollectionReference chatRef=mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString());

        String newID = chatRef.document().getId();
        message.setStatus(DB.CHAT_STATUS.SENT.toString());
        message.setMessageUID(newID);
        message.setTimestamp(System.currentTimeMillis());
        message.setMessage(message.getMessage().trim());
        chatRef.document(newID)
                .set(message)
                .addOnSuccessListener(unused -> {
                    isSuccess.accept(true);
                    Log.e("sendMessage: ","sent" );
                    updateConversation(conversationID,message);
                    createNewReadMark(conversationID,message.getSenderId());
                    sendNotification(new NotificationBody(
                            friendToken,
                            "Tin nhắn mới",
                            message.getMessage(),
                            message.getSenderId(),
                            conversationID,
                            "null"
                    ));
                })
                .addOnFailureListener(e -> {
                    Log.e("sendMessage: ",e.getMessage() );
                    message.setStatus(DB.CHAT_STATUS.FAILED.toString());
                    isSuccess.accept(false);

                });

    }
    public static void addToReadMark(String conversationID, String usrUID){
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationID)
                .update(DB.CONVERSATIONS_COLLECTION.READ_LIST.toString(), FieldValue.arrayUnion(usrUID));
}
    public static void createNewReadMark(String conversationID, String senderUID){
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationID)
                .update(DB.CONVERSATIONS_COLLECTION.READ_LIST.toString(), Collections.singletonList(senderUID));
    }
    private static void updateConversation(String conversationsUID,Message chat){
        if (conversationsUID == null || conversationsUID.isEmpty()) {
            Log.e("updateConversation", "Lỗi: conversationsUID ");
            return;
        }
        Map<String,Object> data = new HashMap<>();
        data.put(DB.CONVERSATIONS_COLLECTION.LAST_UPDATED_AT.toString(),System.currentTimeMillis());
        Map<String,Object> subData = new HashMap<>();
        subData.put(DB.MESSAGES_COLLECTION.MESSAGE.toString(),chat.getMessage());
        subData.put(DB.MESSAGES_COLLECTION.TYPE.toString(),chat.getType());
        subData.put(DB.MESSAGES_COLLECTION.REPLIED_TO_MESSAGE_UID.toString(),chat.getRepliedToMessageUID());
        subData.put(DB.MESSAGES_COLLECTION.CHAT_UID.toString(),chat.getMessageUID());
        subData.put(DB.MESSAGES_COLLECTION.SENDER_ID.toString(),chat.getSenderId());
        subData.put(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(),chat.getTimestamp());
        data.put(DB.CONVERSATIONS_COLLECTION.LAST_MESSAGE.toString(),subData);
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.e("updateConversation: ", "ok");
                })
                .addOnFailureListener(e -> {
                    Log.e( "updateConversation: ",e.getMessage() );
                });
    }

    public static MutableLiveData<List<Message>> getMessageList(String conversationsUID){
        MutableLiveData<List<Message>> data= new MutableLiveData<>();
        List<Message> list = new ArrayList<>();

        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString())
                .orderBy(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(), Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                        Message message = documentSnapshot.toObject(Message.class);
                        if(message!=null)list.add(message);
                    }

                    Collections.reverse(list);
                    data.postValue(list);
                    list.forEach(message -> {
                        Log.e("getMessageList: ",message.getMessage()+"///"+ TimeUtils.getTimeAgo(message.getTimestamp()));
                    });
                    listenForNewMessage(conversationsUID,list,data);

                });
        return data;
    }
    public static void updatelastSeen(String conversationID, String userUID, long lastMSGTimeStamp){
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationID)
                .update(DB.CONVERSATIONS_COLLECTION.LAST_SEEN.toString()+"."+userUID,lastMSGTimeStamp);
    }
    public static LiveData<Map<String, Long>> getLastSeenMap (String conversationID){
        MutableLiveData<Map<String,Long>> data = new MutableLiveData<>();
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationID)
                .addSnapshotListener((value, error) -> {
                    Map<String,Long> dataMap =(Map<String, Long>) value.get(DB.CONVERSATIONS_COLLECTION.LAST_SEEN.toString());

                    if(dataMap!=null) data.postValue(dataMap);
                    else data.postValue(new HashMap<>());
                });
        return data;
    }
    private static void listenForNewMessage(String conversationsUID, List<Message> list,MutableLiveData<List<Message>> data) {
        mFirebase.collection(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .document(conversationsUID)
                .collection(DB.MESSAGES_COLLECTION.NAME.toString())
                .orderBy(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(),Query.Direction.ASCENDING)
                .whereGreaterThan(DB.MESSAGES_COLLECTION.TIMESTAMP.toString(),System.currentTimeMillis())
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if(error!=null){
                        Log.e("listenForNewMessage: ", error.getMessage());
                        return;
                    }
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    List<Message> newMessages = new ArrayList<>();
                    for(DocumentChange change: queryDocumentSnapshots.getDocumentChanges()){
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message newMessage = change.getDocument().toObject(Message.class);
                                newMessages.add(newMessage);
                        }
                    }
                    if (!newMessages.isEmpty()) {
                        list.addAll(newMessages);
                        data.postValue(new ArrayList<>(list));
                        Log.e("listenForNewMessage: ",newMessages.size()+"" );
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

    public static MutableLiveData<Boolean> getTypingStatus(String conversationID,String partnerUID) {
        MutableLiveData<Boolean> data = new MutableLiveData<>(false);
        DatabaseReference userStatusRef = mFireDB.getReference(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .child(conversationID)
                .child(partnerUID)
                .child("isTyping");


        userStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean status = snapshot.getValue(Boolean.TYPE);
                    data.postValue(status);
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
    public static void setTypingStatus(String userUID,String conversationID,boolean isTyping) {
        Log.e("setIsUserTyping2: ", isTyping?"type":"no");
        if(userUID.isEmpty()||conversationID.isEmpty()) return;
        mFireDB.getReference(DB.CONVERSATIONS_COLLECTION.NAME.toString())
                .child(conversationID)
                .child(userUID)
                .child("isTyping")
                .setValue(isTyping)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase", "Typing status updated successfully");
                    } else {
                        Log.e("Firebase", "Failed to update typing status", task.getException());
                    }
                });

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

    private static void sendNotification(NotificationBody body){
        NotificationAPI notificationAPI = new NotificationAPI();
        API api = notificationAPI.createRetrofitClass(API.class);
        api.sendNotification(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    if (response.code()==200){
                        Log.e("sendNotification: ", "sent");
                    }else {
                        Log.e("sendNotification: ", "not sent "+ response.code());
                    }
                }else {
                    Log.e("sendNotification: ", "not sent "+ response.raw());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.e("sendNotification: ", "not sent "+ throwable.getMessage());
            }
        });

    }


}
