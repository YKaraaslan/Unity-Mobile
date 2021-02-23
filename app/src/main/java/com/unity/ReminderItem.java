package com.unity;

import java.io.Serializable;

public class ReminderItem implements Serializable {
    public int id;
    public String title, note, date_created, time_created, date_end, time_end, situation;

    public ReminderItem () {}

    public ReminderItem(int id, String title, String note, String date_created, String time_created, String date_end, String time_end, String situation) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.date_created = date_created;
        this.time_created = time_created;
        this.date_end = date_end;
        this.time_end = time_end;
        this.situation = situation;
    }

    public String getSituation() {
        return situation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getTime_created() {
        return time_created;
    }

    public void setTime_created(String time_created) {
        this.time_created = time_created;
    }

    public String getDate_end() {
        return date_end;
    }

    public void setDate_end(String date_end) {
        this.date_end = date_end;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }
}
