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
        FRIEND_UID_LIST{
            @NonNull
            @Override
            public String toString() {
                return "friend_uid_list";
            }
        },
        UID{
            @NonNull
            @Override
            public String toString() {
                return super.toString();
            }
        },
        TOKEN{
            @NonNull
            @Override
            public String toString() {
                return "token";
            }
        }

    }
    public enum FRIEND_LIST_COLLECTION{
        NAME{
            @NonNull
            @Override
            public String toString() {
                return "friendlist";
            }
        },
        RECEIVER_ID{
            @NonNull
            @Override
            public String toString() {
                return "receiverID";
            }
        },
        SENDER_ID{
            @NonNull
            @Override
            public String toString() {
                return "senderID";
            }
        },
        STATUS{
            @NonNull
            @Override
            public String toString() {
                return "status";
            }
        }
    }
    public enum  CONVERSATIONS_COLLECTION{
        NAME{
            @NonNull
            @Override
            public String toString() {
                return "conversations";
            }
        },

        PARTICIPANT_IDS{
            @NonNull
            @Override
            public String toString() {
                return "participantIds";
            }
        },
        INITIATED_AT{
            @NonNull
            @Override
            public String toString() {
                return "initiatedAt";
            }
        },
        INITIATEDBY{
            @NonNull
            @Override
            public String toString() {
                return "initiatedBy";
            }
        },
        LAST_MESSAGE{
            @NonNull
            @Override
            public String toString() {
                return "lastMessage";
            }
        },
        LAST_UPDATED_AT{
            @NonNull
            @Override
            public String toString() {
                return "lastUpdatedAt";
            }
        },
        DOCUMMENT_ID{
            @NonNull
            @Override
            public String toString() {
                return "documentId";
            }
        }









    }
    public enum MESSAGES_COLLECTION{
        NAME{
            @NonNull
            @Override
            public String toString() {
                return "messages";
            }
        },
        MESSAGE {
            @NonNull
            @Override
            public String toString() {
                return "message";
            }
        },
        TYPE {
            @NonNull
            @Override
            public String toString() {
                return "type";
            }
        },
        SENDER_ID {
            @NonNull
            @Override
            public String toString() {
                return "senderId";
            }
        },
        STATUS {
            @NonNull
            @Override
            public String toString() {
                return "status";
            }
        },
        TIMESTAMP {
            @NonNull
            @Override
            public String toString() {
                return "timestamp";
            }
        },
        REPLIED_TO_MESSAGE_UID {
            @NonNull
            @Override
            public String toString() {
                return "repliedToMessageUID";
            }
        },
        CHAT_UID{
            @NonNull
            @Override
            public String toString() {
                return "messageUID";
            }
        }

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

    public enum CHAT_STATUS{
        SENDING{
            @NonNull
            @Override
            public String toString() {
                return "sending";
            }
        },
        SENT{
            @NonNull
            @Override
            public String toString() {
                return "sent";
            }
        },
        FAILED{
            @NonNull
            @Override
            public String toString() {
                return "failed";
            }
        },
        READ{
            @NonNull
            @Override
            public String toString() {
                return "read";
            }
        },


    }
}
