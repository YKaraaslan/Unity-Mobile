package com.unity;

import java.io.Serializable;

public class ListOfPeopleAssigningItems implements Serializable {
    public int id, userID;
    public String username, date, time, assignedBy;
    public int assignedByID;
    public boolean deleted;
    public String date_process, time_process, seen_date, seen_time;
    public boolean seen_situation;

    public ListOfPeopleAssigningItems(int id, int userID, String username, String date, String time,
                                      String assignedBy, int assignedByID, boolean deleted, String date_process, String time_process, String seen_date, String seen_time, boolean seen_situation) {
        this.id = id;
        this.userID = userID;
        this.username = username;
        this.date = date;
        this.time = time;
        this.assignedBy = assignedBy;
        this.assignedByID = assignedByID;
        this.deleted = deleted;
        this.date_process = date_process;
        this.time_process = time_process;
        this.seen_date = seen_date;
        this.seen_time = seen_time;
        this.seen_situation = seen_situation;
    }

    public int getId() {
        return id;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public int getAssignedByID() {
        return assignedByID;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getDate_process() {
        return date_process;
    }

    public String getTime_process() {
        return time_process;
    }

    public String getSeen_date() {
        return seen_date;
    }

    public String getSeen_time() {
        return seen_time;
    }

    public boolean isSeen_situation() {
        return seen_situation;
    }
}
