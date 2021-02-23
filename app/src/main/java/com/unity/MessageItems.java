package com.unity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.io.Serializable;

public class MessageItems implements Serializable {
    public String message;
    public int senderID, receiverID;
    public boolean photo, document, read;
    public String time_sent, time_read;
    public FieldValue time_server;
    public boolean new_date;
    public String situation, time_received;
    public boolean received;

    public MessageItems(){
    }

    public MessageItems(String message, int senderID, int receiverID, boolean photo, boolean document, boolean read,
                        String time_sent, String time_read, FieldValue time_server, boolean new_date, String situation, String time_received, boolean received) {
        this.message = message;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.photo = photo;
        this.document = document;
        this.read = read;
        this.time_sent = time_sent;
        this.time_read = time_read;
        this.time_server = time_server;
        this.new_date = new_date;
        this.situation = situation;
        this.time_received = time_received;
        this.received = received;
    }

    public boolean isReceived() {
        return received;
    }

    public String getSituation() {
        return situation;
    }

    public boolean isNew_date() {
        return new_date;
    }

    public String getMessage() {
        return message;
    }

    public int getSenderID() {
        return senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public boolean isPhoto() {
        return photo;
    }

    public boolean isDocument() {
        return document;
    }

    public boolean isRead() {
        return read;
    }

    public String getTime_sent() {
        return time_sent;
    }

    public String getTime_read() {
        return time_read;
    }
}
