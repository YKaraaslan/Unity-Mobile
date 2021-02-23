package com.unity;

import java.io.Serializable;

public class AssignmentsUserDBItems implements Serializable {
    public String title, note, status, date, time, finish_date, finish_time, finish_note;
    public int assignmentID, inChargeID, createdByID;

    public AssignmentsUserDBItems () { }

    public AssignmentsUserDBItems(String title, String note, String status, String date, String time, String finish_date, String finish_time,
                                  String finish_note, int assignmentID, int inChargeID, int createdByID) {
        this.title = title;
        this.note = note;
        this.status = status;
        this.date = date;
        this.time = time;
        this.finish_date = finish_date;
        this.finish_time = finish_time;
        this.finish_note = finish_note;
        this.assignmentID = assignmentID;
        this.inChargeID = inChargeID;
        this.createdByID = createdByID;
    }

    public String getFinish_date() {
        return finish_date;
    }

    public String getFinish_time() {
        return finish_time;
    }

    public int getCreatedByID() {
        return createdByID;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFinish_note() {
        return finish_note;
    }

    public int getAssignmentID() {
        return assignmentID;
    }

    public int getInChargeID() {
        return inChargeID;
    }
}
