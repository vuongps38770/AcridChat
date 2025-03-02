package com.example.acrid.Model;

public class NotificationBody {
    private String token="",title="",body="",senderId="", chatId="",destinationScreen="", messageType="",senderName="";

    public NotificationBody(String token, String title, String body, String senderId,String senderName, String chatId,String messageType, String destinationScreen) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.senderId = senderId;
        this.chatId = chatId;
        this.messageType = messageType;
        this.senderName = senderName;
        this.destinationScreen = destinationScreen;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getDestinationScreen() {
        return destinationScreen;
    }

    public void setDestinationScreen(String destinationScreen) {
        this.destinationScreen = destinationScreen;
    }
}
