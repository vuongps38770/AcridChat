package com.example.acrid.Model;

public class Message {
    private String messageUID;
    private String message;
    private String type;
    private String senderId,status;
    private long timestamp;
    private String repliedToMessageUID;

    public Message( String message, String type, String senderId, String status, String repliedToMessageUID) {
        this.message = message;
        this.type = type;
        this.senderId = senderId;
        this.status = status;
        this.repliedToMessageUID = repliedToMessageUID;
    }

    public Message() {
    }

    public String getMessageUID() {
        return messageUID;
    }

    public void setMessageUID(String messageUID) {
        this.messageUID = messageUID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRepliedToMessageUID() {
        return repliedToMessageUID;
    }

    public void setRepliedToMessageUID(String repliedToMessageUID) {
        this.repliedToMessageUID = repliedToMessageUID;
    }
}
