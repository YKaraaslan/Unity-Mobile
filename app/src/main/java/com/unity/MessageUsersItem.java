package com.unity;

import java.io.Serializable;

public class MessageUsersItem implements Serializable {
    private int id;
    private String name, description, online;
    private int Image;

    public MessageUsersItem(){

    }

    public MessageUsersItem(int id, String name, String description, String online, int image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.online = online;
        Image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOnline() {
        return online;
    }

    public int getImage() {
        return Image;
    }
}
