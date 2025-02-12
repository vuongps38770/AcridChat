package com.example.acrid.Model;

import java.util.List;

public class Conversation {
    private String documentId="";
    private long lastUpdatedAt;
    private List<String> participantIds;
    private String lastMessage="";
    private Friend partner;

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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
