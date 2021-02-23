package com.unity;

import java.io.Serializable;

public class AssignmentsCreateUserItems  implements Serializable {
    public int id;
    public String date, time, status;


    public AssignmentsCreateUserItems(int id, String date, String time, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.status = status;
    }
}
