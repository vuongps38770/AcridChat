package com.example.acrid.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.ChatRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    public MutableLiveData<List<Message>> chatData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Boolean> isChatScreenActive  = new MutableLiveData<>(false);
    public MutableLiveData<String> conversationsID = new MutableLiveData<>("");
    public MutableLiveData<String> senderID = new MutableLiveData<>("");
    public MutableLiveData<String> message = new MutableLiveData<>("");
    public MutableLiveData<String> type = new MutableLiveData<>("text");
    public MutableLiveData<String> replyTo = new MutableLiveData<>("");
    public MutableLiveData<Boolean> partnerStatus = new MutableLiveData<>(false);
    public MutableLiveData<String> partnerUID = new MutableLiveData<>("");
    public MutableLiveData<Map<String,Long>> lastSeenMap = new MutableLiveData<>();
    public MutableLiveData<Boolean> isPartnerTyping = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isUserTyping = new MutableLiveData<>(false);
    private final LiveData<Boolean> isReady = Transformations.switchMap(conversationsID, convID ->
            Transformations.map(partnerUID, uid ->
                    convID != null && !convID.isEmpty() && uid != null && !uid.isEmpty()
            )
    );
    public void setIsUserTyping(boolean isTyping){
        Log.e("setIsUserTyping: ", isTyping?"type":"no");
        isUserTyping.postValue(isTyping);
    }


    public ChatViewModel() {
        senderID.postValue(UserRepo.getCurrentUserUID());
        observeConversation();
        initUserTyping();
    }

    private void initUserTyping() {
        isUserTyping.observeForever(isTyping -> {
            if (senderID.getValue() != null && conversationsID.getValue() != null) {
                ChatRepo.setTypingStatus(senderID.getValue(), conversationsID.getValue(), isTyping);
            }
        });
    }

    private void initLastSeenMap(String chatUID){
        ChatRepo.getLastSeenMap(chatUID).observeForever(stringLongMap -> {
            lastSeenMap.postValue(stringLongMap);
        });
    }
    private void initPartnerTypingStatus(String convID,String partnerUID){
        ChatRepo.getTypingStatus(convID,partnerUID).observeForever(aBoolean -> {
            Log.e("initPartnerTypingStatus: ",aBoolean.toString() );
            isPartnerTyping.postValue(aBoolean);
        });
    }
    private void observeConversation() {
        conversationsID.observeForever(id -> {
            if (id != null && !id.isEmpty()&&id.equals(this.conversationsID.getValue())) {
                initChatData(id);
                initLastSeenMap(id);
            }
        });
        partnerUID.observeForever(string -> {
            if(string!=null&&!string.isEmpty()){
                updateStatus();
            }
        });
        isReady.observeForever(isValid -> {
            if (Boolean.TRUE.equals(isValid)) {
                initPartnerTypingStatus(conversationsID.getValue(), partnerUID.getValue());
            }
        });


    }
    public void addToReadMark(){
        ChatRepo.addToReadMark(conversationsID.getValue(),UserRepo.getCurrentUserUID());
    }

    private void updateStatus() {
        ChatRepo.getStatus(partnerUID.getValue()).observeForever(isOnline -> {
            Log.e("updateStatus: ",isOnline.toString() );
            partnerStatus.setValue(isOnline);
        });
    }
    int time =1;
    private void initChatData(String conversationsID){
        Log.e("ChatViewModel", "initChatData called with ID: " + conversationsID);
        ChatRepo.getMessageList(conversationsID).observeForever(messages -> {
            Log.e("initChatData: ", "lần "+time++);
            chatData.postValue(messages);
            if (messages != null && !messages.isEmpty()&& Boolean.TRUE.equals(isChatScreenActive.getValue())) {
                long lastMessageTimeStamp= messages.get(messages.size() - 1).getTimestamp();
                ChatRepo.updatelastSeen(conversationsID, UserRepo.getCurrentUserUID(),lastMessageTimeStamp);
                addToReadMark();
            }
        });
    }

    public void sendMessage(String partnerToken){
        if(senderID.getValue().isEmpty()||type.getValue().isEmpty()||message.getValue().trim().isEmpty()) return;


        boolean isHaveReplyTo = replyTo.getValue()!=null&&!replyTo.getValue().isEmpty();
        Message newMess=new Message(message.getValue(),
                type.getValue(),senderID.getValue(),
                DB.CHAT_STATUS.SENDING.toString(),
                isHaveReplyTo?replyTo.getValue():null);
        message.setValue("");
        List<Message> list = chatData.getValue();
        if (list == null) {
            list = new ArrayList<>();
        }


        List<Message> finalList = list;
        ChatRepo.sendMessage(conversationsID.getValue(),
                newMess,
                aBoolean -> {
                            if (!aBoolean) {
                                newMess.setStatus(DB.CHAT_STATUS.FAILED.toString());
                                finalList.add(newMess);
                                chatData.setValue(new ArrayList<>(finalList));

//                            }else {
//                                FireBaseService.sendMessage(partnerToken,"tin nhắn mới",newMess.getMessage());
////                                try {
//////                                    FireBaseService.sendNotification(partnerToken,"tin nhắn mới",newMess.getMessage());
////                                } catch (IOException e) {
////                                    Log.e( "sendMessage: ",e.getMessage() );
////                                }
                            }

                        });
    }

    public void loadOlderMessages() {
        String conversationId = conversationsID.getValue();
        if (conversationId == null || conversationId.isEmpty()) return;

        List<Message> currentMessages = chatData.getValue();
        long lastMessageTimestamp = (currentMessages != null && !currentMessages.isEmpty())
                ? currentMessages.get(0).getTimestamp()
                : -1;
        if (lastMessageTimestamp==-1){
            return;
        }
        ChatRepo.getPreviousMessages(conversationId,lastMessageTimestamp , oldMessages -> {
            if (oldMessages != null && !oldMessages.isEmpty()) {
                for (Message message1:oldMessages){
                    Log.e("loadOlderMessages: ",message1.getTimestamp()+"" );

                }
                List<Message> updatedMessages = new ArrayList<>(oldMessages);
                updatedMessages.addAll(currentMessages);
                chatData.postValue(updatedMessages);
            }
        });
    }
}
