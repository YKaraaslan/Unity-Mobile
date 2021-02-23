package com.unity;

import java.io.Serializable;

public class ListOfPeopleItems implements Serializable {
    private int id;
    private String name;

    public ListOfPeopleItems(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}