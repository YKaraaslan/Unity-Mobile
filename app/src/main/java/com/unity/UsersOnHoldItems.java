package com.unity;

import java.io.Serializable;

public class UsersOnHoldItems implements Serializable {
    public String name, mail, phone, username, password, position, date_applied, time_applied, date_result, time_result;
    public int authorization, id;
    public boolean worker;
    public String online;
    public String handledBy;
    public int handledByID;

    public UsersOnHoldItems(String name, String mail, String phone, String username, String password, String position, String date_applied, String time_applied,
                            String date_result, String time_result, int authorization, int id, boolean worker, String online, String handledBy, int handledByID) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDate_applied() {
        return date_applied;
    }

    public void setDate_applied(String date_applied) {
        this.date_applied = date_applied;
    }

    public String getTime_applied() {
        return time_applied;
    }

    public void setTime_applied(String time_applied) {
        this.time_applied = time_applied;
    }

    public String getDate_result() {
        return date_result;
    }

    public void setDate_result(String date_result) {
        this.date_result = date_result;
    }

    public String getTime_result() {
        return time_result;
    }

    public void setTime_result(String time_result) {
        this.time_result = time_result;
    }

    public int getAuthorization() {
        return authorization;
    }

    public void setAuthorization(int authorization) {
        this.authorization = authorization;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isWorker() {
        return worker;
    }

    public void setWorker(boolean worker) {
        this.worker = worker;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    public int getHandledByID() {
        return handledByID;
    }

    public void setHandledByID(int handledByID) {
        this.handledByID = handledByID;
    }
}

