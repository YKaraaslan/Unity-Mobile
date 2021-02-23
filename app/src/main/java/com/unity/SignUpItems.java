package com.unity;

public class SignUpItems {
    public String name, mail, phone, username, password, position, date_applied, time_applied, date_result, time_result;
    public int authorization, id;
    public boolean worker;
    public String online;
    public String handledBy;
    public int handledByID;

    public SignUpItems(String name, String mail, String phone, String username, String password, String position, String date_applied,
                       String time_applied, String date_result, String time_result, int authorization, int id, boolean worker, String online, String handledBy, int handledByID) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.position = position;
        this.date_applied = date_applied;
        this.time_applied = time_applied;
        this.date_result = date_result;
        this.time_result = time_result;
        this.authorization = authorization;
        this.id = id;
        this.worker = worker;
        this.online = online;
        this.handledBy = handledBy;
        this.handledByID = handledByID;
    }
}
