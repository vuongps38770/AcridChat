package com.example.acrid.Model;

public class ChatBundle {
    private String conversationID;
    private String friendID;

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
