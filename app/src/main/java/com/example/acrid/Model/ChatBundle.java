package com.example.acrid.Model;

import androidx.annotation.NonNull;

import java.util.Map;

public class ChatBundle {
    private String conversationID;
    private String friendID;
    private String friendToken;
    private Map<String,Long> lastSeenMap;

    public ChatBundle(@NonNull String conversationID,@NonNull String friendID,@NonNull String friendToken,@NonNull Map<String, Long> lastSeenMap) {
        this.conversationID = conversationID;
        this.friendID = friendID;
        this.friendToken = friendToken;
        this.lastSeenMap = lastSeenMap;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }

    public ChatBundle(String conversationID, String friendID) {
        this.conversationID = conversationID;
        this.friendID = friendID;
    }
}
