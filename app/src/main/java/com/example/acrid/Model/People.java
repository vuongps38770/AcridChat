package com.example.acrid.Model;

import java.util.List;

public class People extends BasePeople{
    private String userUID,displayName,IDName,email,profileIMG,birthDay,description;
    private long createdDate;
    private String status;
    private String friendStatus = "";
    private List<String> friend_uid_list;

    public List<String> getFriend_uid_list() {
        return friend_uid_list;
    }

    public void setFriend_uid_list(List<String> friend_uid_list) {
        this.friend_uid_list = friend_uid_list;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIDName() {
        return IDName;
    }

    public void setIDName(String IDName) {
        this.IDName = IDName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileIMG() {
        return profileIMG;
    }

    public void setProfileIMG(String profileIMG) {
        this.profileIMG = profileIMG;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
