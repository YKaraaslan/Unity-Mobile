package com.unity;

import java.io.Serializable;

public class FragmentContactItem implements Serializable {
    public String user_name, last_message, time, time_read, dbID;
    public int userID, senderID;
    public boolean read;
    public String situation;

    public FragmentContactItem() {
    }

    public FragmentContactItem(String user_name, String last_message, String time, String time_read, String dbID, int userID, int senderID, boolean read, String situation) {
        this.user_name = user_name;
        this.last_message = last_message;
        this.time = time;
        this.time_read = time_read;
        this.dbID = dbID;
        this.userID = userID;
        this.senderID = senderID;
        this.read = read;
        this.situation = situation;
    }

    public String getSituation() {
        return situation;
    }

    public int getSenderID() {
        return senderID;
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
}
