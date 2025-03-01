package com.example.acrid.Model;

import android.app.Application;

public class GlobalData extends Application {
    private static GlobalData instance;
    String curentConversationID;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public String getCurentConversationID() {
        return curentConversationID;
    }

    public void setCurentConversationID(String curentConversationID) {
        this.curentConversationID = curentConversationID;
    }

    public static GlobalData getInstance() {
        return instance;
    }
}
