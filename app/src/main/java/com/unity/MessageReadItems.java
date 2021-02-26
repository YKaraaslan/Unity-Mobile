package com.unity;

public class MessageReadItems {
    public String message;
    public int senderID, receiverID;
    public boolean photo, document, read;
    public String time_sent, time_read;
    public boolean new_date;
    public String messageID, situation;
    public boolean received;

    public MessageReadItems() { }

    public MessageReadItems(String message, int senderID, int receiverID, boolean photo, boolean document,
                            boolean read, String time_sent, String time_read, boolean new_date, String messageID, String situation, boolean received) {
        this.message = message;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.photo = photo;
        this.document = document;
        this.read = read;
        this.time_sent = time_sent;
        this.time_read = time_read;
        this.new_date = new_date;
        this.messageID = messageID;
        this.situation = situation;
        this.received = received;
    }

    public boolean isReceived() {
        return received;
    }

    public String getSituation() {
        return situation;
    }

    public String getMessageID() {
        return messageID;
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
