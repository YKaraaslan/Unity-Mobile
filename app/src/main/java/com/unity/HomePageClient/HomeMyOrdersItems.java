package com.unity.HomePageClient;

public class HomeMyOrdersItems {
    private int id;
    private String name, description, time;
    private int Image;

    public HomeMyOrdersItems(int id, String name, String description, String time, int image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.time = time;
        this.Image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getImage() {
        return Image;
    }

    public String getDescription() {
        return description;
    }
}
