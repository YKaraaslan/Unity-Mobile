package com.unity.HomePageClient;

public class HomeMyOrdersItems {
    private final int id;
    private final String name;
    private final String description;
    private final String time;
    private final int Image;

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
