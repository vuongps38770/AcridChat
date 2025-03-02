package com.example.acrid.viewModel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.ChatRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.GlobalData;
import com.example.acrid.Model.Message;
import com.example.acrid.View.Dialog.ImageDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    public MutableLiveData<List<Message>> chatData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Boolean> isChatScreenActive  = new MutableLiveData<>(false);
    public MutableLiveData<String> conversationsID = new MutableLiveData<>("");
    public MutableLiveData<String> senderID = new MutableLiveData<>("");
    public MutableLiveData<String> parnerName = new MutableLiveData<>("");
    public MutableLiveData<String> message = new MutableLiveData<>("");
    public MutableLiveData<String> type = new MutableLiveData<>("text");
    public MutableLiveData<String> errorMessage = new MutableLiveData<>("");
    public MutableLiveData<String> replyTo = new MutableLiveData<>("");
    public MutableLiveData<Boolean> partnerStatus = new MutableLiveData<>(false);
    public MutableLiveData<String> partnerUID = new MutableLiveData<>("");
    public MutableLiveData<Map<String,Long>> lastSeenMap = new MutableLiveData<>();
    public MutableLiveData<Boolean> isPartnerTyping = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isUserTyping = new MutableLiveData<>(false);
    public MutableLiveData<File> image= new MutableLiveData<>();
    public MutableLiveData<Integer> notifiPos= new MutableLiveData<>(-1);

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
        obseveImage();
    }

    private void obseveImage() {
        image.observeForever(file -> {
            if(file==null) type.postValue(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString());
            else type.postValue(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.IMAGE.toString());
        });
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
                GlobalData.getInstance().setCurentConversationID(id);
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
        Log.e("addToReadMark: ","added" );
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
        ChatRepo.getMessageList(conversationsID).observeForever(messages -> {
            chatData.setValue(messages);
            if (messages != null && !messages.isEmpty()&& Boolean.TRUE.equals(isChatScreenActive.getValue())) {
                long lastMessageTimeStamp= messages.get(messages.size() - 1).getTimestamp();
                ChatRepo.updatelastSeen(conversationsID, UserRepo.getCurrentUserUID(),lastMessageTimeStamp);
                addToReadMark();
            }
        });
    }


    public void sendMessage(String partnerToken){
        if(type.getValue().equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString())){
            sendTextMessage(partnerToken);
        }else if (type.getValue().equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.IMAGE.toString())){
            sendImageMessage(partnerToken);
        }
    }

    private void sendTextMessage(String partnerToken) {
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
                newMess,partnerToken
                ,parnerName.getValue(),
                aBoolean -> {
                    if (!aBoolean) {

                        newMess.setStatus(DB.CHAT_STATUS.FAILED.toString());
                        finalList.add(newMess);
                        notifiPos.setValue(finalList.indexOf(newMess));
                        Log.e("sendTextMessage: ", finalList.indexOf(newMess)+"");
                        chatData.setValue(new ArrayList<>(finalList));
                    }else {
                        Log.e("sendTextMessage: ", newMess.getStatus());
                        newMess.setStatus(DB.CHAT_STATUS.SENT.toString());
                        notifiPos.setValue(finalList.indexOf(newMess));
                    }

                });
    }

    private void sendImageMessage(String partnerToken){
        if(senderID.getValue().isEmpty()||type.getValue().isEmpty()||image==null) return;
        boolean isHaveReplyTo = replyTo.getValue()!=null&&!replyTo.getValue().isEmpty();
        Message newMess=new Message(message.getValue(),
                type.getValue(),senderID.getValue(),
                DB.CHAT_STATUS.SENDING.toString(),
                isHaveReplyTo?replyTo.getValue():null);



        ChatRepo.sendImageSingle(image.getValue(), new ChatRepo.SendImageCallBack() {
            @Override
            public void onSuccess(String imgUrl) {
                image.postValue(null);
                type.postValue(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString());
                Log.e( "onSuccess: ", imgUrl);
                Message newMess=new Message(imgUrl,
                        type.getValue(),senderID.getValue(),
                        DB.CHAT_STATUS.SENDING.toString(),
                        isHaveReplyTo?replyTo.getValue():null);
                List<Message> list = chatData.getValue();
                if (list == null) {
                    list = new ArrayList<>();
                }

                List<Message> finalList = list;
                ChatRepo.sendMessage(conversationsID.getValue(),
                        newMess,partnerToken
                        ,parnerName.getValue(),
                        aBoolean -> {
                            if (!aBoolean) {
                                newMess.setStatus(DB.CHAT_STATUS.FAILED.toString());
                                finalList.add(newMess);
                                chatData.setValue(new ArrayList<>(finalList));
                            }
                        });


            }

            @Override
            public void onLoading(boolean onload) {

            }

            @Override
            public void onFailure(String err) {
                image.setValue(null);
                type.setValue(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString());
                Log.e( "onFail: ", err);
                errorMessage.setValue("Lỗi, không thể gửi ảnh");

            }

            @Override
            public void onError() {
                Log.e( "onSuccess: ", "fail");
                image.setValue(null);
                type.setValue(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString());

            }
        });
    }

    public void loadOlderMessages(Consumer<Boolean> isHasMoreMSG) {
        String conversationId = conversationsID.getValue();
        if (conversationId == null || conversationId.isEmpty()) return;

        List<Message> currentMessages = chatData.getValue();
        long lastMessageTimestamp = (currentMessages != null && !currentMessages.isEmpty())
                ? currentMessages.get(0).getTimestamp()
                : -1;
        if (lastMessageTimestamp==-1){
            return;
        }
        ChatRepo.getPreviousMessages(conversationId,lastMessageTimestamp , (oldMessages,isHasMore) -> {
            isHasMoreMSG.accept(isHasMore);
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

    public void clickItem(FragmentManager manager, Message message) {
        if(message.getType().equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.IMAGE.toString())){
            ImageDialogFragment dialogFragment = new ImageDialogFragment(message.getMessage());
            dialogFragment.show(manager,"image_dialog");
        }

    }
}
