package com.example.acrid.Model;

import java.util.ArrayList;
import java.util.List;

public class User extends BasePeople{
    private String userUID="",displayName="",IDName="",email="",profileIMG="",birthDay="",token="", description="",status="";
    long createdDate;
    private List<String> friend_uid_list = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserUID() {
        return userUID;
    }

    public List<String> getFriend_uid_list() {
        return friend_uid_list;
    }

    public void setFriend_uid_list(List<String> friend_uid_list) {
        this.friend_uid_list = friend_uid_list;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIDName() {
        return IDName;
    }

    public void setIDName(String IDName) {
        this.IDName = IDName;
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

    @Override
    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}
