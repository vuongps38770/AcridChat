package com.example.acrid.Constant;

import androidx.annotation.NonNull;

public enum DB {;
    public enum USER_COLLECTION{
        NAME{
            @NonNull
            @Override
            public String toString() {
                return "users";
            }
        },
        ID_NAME{
            @NonNull
            @Override
            public String toString() {
                return "idname";
            }
        },
        EMAIL{
            @NonNull
            @Override
            public String toString() {
                return "email";
            }
        },

    }
    public enum FRIEND_LIST_COLLECTION{
        NAME{
            @NonNull
            @Override
            public String toString() {
                return "friendList";
            }
        };
    }




    public enum FRIEND_STATUS{
        PENDING{
            @NonNull
            @Override
            public String toString() {
                return "pending";
            }
        }
        ,ACCEPTED{
            @NonNull
            @Override
            public String toString() {
                return "accepted";
            }
        }
        ,NONE{
            @NonNull
            @Override
            public String toString() {
                return "none";
            }
        }
    }
}
