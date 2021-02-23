package com.unity;

public class AssignmentsDirectItems {
    public int id, assignedID;
    public String name, seen_date, seen_time;
    public boolean seen_situation;

    public AssignmentsDirectItems(int id, int assignedID, String name, String seen_date, String seen_time, boolean seen_situation) {
        this.id = id;
        this.assignedID = assignedID;
        this.name = name;
        this.seen_date = seen_date;
        this.seen_time = seen_time;
        this.seen_situation = seen_situation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssignedID() {
        return assignedID;
    }

    public void setAssignedID(int assignedID) {
        this.assignedID = assignedID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeen_date() {
        return seen_date;
    }

    public void setSeen_date(String seen_date) {
        this.seen_date = seen_date;
    }

    public String getSeen_time() {
        return seen_time;
    }

    public void setSeen_time(String seen_time) {
        this.seen_time = seen_time;
    }

    public boolean isSeen_situation() {
        return seen_situation;
    }

    public void setSeen_situation(boolean seen_situation) {
        this.seen_situation = seen_situation;
    }
}
