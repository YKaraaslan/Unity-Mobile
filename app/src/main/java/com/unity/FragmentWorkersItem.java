package com.unity;

import java.io.Serializable;

public class FragmentWorkersItem implements Serializable {
    private int id;
    private String name, position, online;
    private int Image;

    public FragmentWorkersItem(){

    }

    public FragmentWorkersItem(int id, String name, String position, String online, int image) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.online = online;
        Image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getOnline() {
        return online;
    }

    public int getImage() {
        return Image;
    }
}
