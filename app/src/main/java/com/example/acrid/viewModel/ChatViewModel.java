package com.example.acrid.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.ChatRepo;
import com.example.acrid.Helper.FireBaseService;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    public MutableLiveData<List<Message>> chatData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<String> conversationsID = new MutableLiveData<>("");
    public MutableLiveData<String> senderID = new MutableLiveData<>("");
    public MutableLiveData<String> message = new MutableLiveData<>("");
    public MutableLiveData<String> type = new MutableLiveData<>("text");
    public MutableLiveData<String> replyTo = new MutableLiveData<>("");
    public MutableLiveData<Boolean> partnerStatus = new MutableLiveData<>(false);
    public MutableLiveData<String> partnerUID = new MutableLiveData<>("");
    public ChatViewModel() {
        observeConversationID();
        senderID.setValue(UserRepo.getCurrentUserUID());
    }


    private void observeConversationID() {
        conversationsID.observeForever(id -> {
            if (id != null && !id.isEmpty()) {
                initChatData(id);
            }
        });
        partnerUID.observeForever(string -> {
            if(string!=null&&!string.isEmpty()){
                updateStatus();
            }
        });

    }

    private void updateStatus() {
        ChatRepo.getStatus(partnerUID.getValue()).observeForever(isOnline -> {
            partnerStatus.setValue(isOnline);
        });
    }

    private void initChatData(String conversationsID){
        ChatRepo.getMessageList(conversationsID,chatData);
    }

    public void sendMessage(String partnerToken){
        if(senderID.getValue().isEmpty()||type.getValue().isEmpty()||message.getValue().isEmpty()) return;


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
                                chatData.setValue(finalList);
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
                chatData.setValue(updatedMessages);
            }
        });
    }
}
