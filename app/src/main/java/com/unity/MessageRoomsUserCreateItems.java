package com.unity;

import com.google.firebase.firestore.FieldValue;

import java.io.Serializable;

public class MessageRoomsUserCreateItems implements Serializable {
    public String user_name, last_message, time, time_read, dbID;
    public int userID, senderID;
    public boolean read, read_by_me;
    public String situation, last_messageID;
    public FieldValue time_server;

    public MessageRoomsUserCreateItems() {
    }

    public MessageRoomsUserCreateItems(String user_name, String last_message, String time, String time_read,
                                       String dbID, int userID, int senderID, boolean read, boolean read_by_me, String situation, String last_messageID, FieldValue time_server) {
        this.user_name = user_name;
        this.last_message = last_message;
        this.time = time;
        this.time_read = time_read;
        this.dbID = dbID;
        this.userID = userID;
        this.senderID = senderID;
        this.read = read;
        this.read_by_me = read_by_me;
        this.situation = situation;
        this.last_messageID = last_messageID;
        this.time_server = time_server;
    }

    public String getLast_messageID() {
        return last_messageID;
    }

    public String getSituation() {
        return situation;
    }

    public int getSenderID() {
        return senderID;
    }

    public FieldValue getTime_server() {
        return time_server;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getLast_message() {
        return last_message;
    }

    public String getTime() {
        return time;
    }

    public String getTime_read() {
        return time_read;
    }

    public String getDbID() {
        return dbID;
    }

    public int getUserID() {
        return userID;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isRead_by_me() {
        return read_by_me;
    }
}
