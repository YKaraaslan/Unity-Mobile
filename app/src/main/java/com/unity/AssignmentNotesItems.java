package com.unity;

public class AssignmentNotesItems {
    public int id, created_by_id;
    public String created_by, position, note, date, time;
    public boolean deleted, updated;
    public String dateProcess, timeProcess;

    public AssignmentNotesItems(int id, int created_by_id, String created_by, String position, String note, String date, String time,
                                boolean deleted, boolean updated, String dateProcess, String timeProcess) {
        this.id = id;
        this.created_by_id = created_by_id;
        this.created_by = created_by;
        this.position = position;
        this.note = note;
        this.date = date;
        this.time = time;
        this.deleted = deleted;
        this.updated = updated;
        this.dateProcess = dateProcess;
        this.timeProcess = timeProcess;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreated_by_id() {
        return created_by_id;
    }

    public void setCreated_by_id(int created_by_id) {
        this.created_by_id = created_by_id;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public String getDateProcess() {
        return dateProcess;
    }

    public void setDateProcess(String dateProcess) {
        this.dateProcess = dateProcess;
    }

    public String getTimeProcess() {
        return timeProcess;
    }

    public void setTimeProcess(String timeProcess) {
        this.timeProcess = timeProcess;
    }
}
