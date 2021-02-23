package com.unity;

import com.google.firebase.firestore.FieldValue;

public class AssignmentsUpdateItems {
    public String updated_by, updated_time;
    public int updated_by_ID;
    public FieldValue time_server;

    public AssignmentsUpdateItems(String updated_by, String updated_time, int updated_by_ID, FieldValue time_server) {
        this.updated_by = updated_by;
        this.updated_time = updated_time;
        this.updated_by_ID = updated_by_ID;
        this.time_server = time_server;
    }
}
