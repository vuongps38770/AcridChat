package com.example.acrid.Model;

public class NotificationBody {
    private String token="",title="",body="",senderId="", chatId="",destinationScreen="";

    public NotificationBody(String token, String title, String body, String senderId, String chatId, String destinationScreen) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.senderId = senderId;
        this.chatId = chatId;
        this.destinationScreen = destinationScreen;
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
