package com.unity;

import java.io.Serializable;

public class AssignmentNotesSeenByItems implements Serializable {
    public int id;
    public String name, position, seen_date, seen_time;

    public AssignmentNotesSeenByItems(int id, String name, String position, String seen_date, String seen_time) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.seen_date = seen_date;
        this.seen_time = seen_time;
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

    public String getSeen_date() {
        return seen_date;
    }

    public String getSeen_time() {
        return seen_time;
    }
}
