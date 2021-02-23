package com.unity;

public class CompanyAddItems {
    public String name, city, district, in_charge_name, mail, phone, position;
    public int id;

    public CompanyAddItems(String name, String city, String district, String in_charge_name, String mail, String phone, String position, int id) {
        this.name = name;
        this.city = city;
        this.district = district;
        this.in_charge_name = in_charge_name;
        this.mail = mail;
        this.phone = phone;
        this.position = position;
        this.id = id;
    }
}
