package com.example.acrid.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversation {
    private String documentId="";
    private long lastUpdatedAt;
    private List<String> participantIds;
    private Message lastMessage;
    private Friend partner;
    private long unreadCount;
    private Map<String,Long> lastSeen;
    private ArrayList<String> readList;

    public ArrayList<String> getReadList() {
        return readList;
    }

    public void setReadList(ArrayList<String> readList) {
        this.readList = readList;
    }

    public Map<String, Long> getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Map<String, Long> lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Conversation() {
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Friend getPartner() {
        return partner;
    }

    public void setPartner(Friend partner) {
        this.partner = partner;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
