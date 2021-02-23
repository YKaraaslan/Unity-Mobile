package com.unity;

public class MessageRoomsItems {
    public int user_created, user_invited;
    public String date_created;

    public MessageRoomsItems(int user_created, int user_invited, String date_created) {
        this.user_created = user_created;
        this.user_invited = user_invited;
        this.date_created = date_created;
    }
}
