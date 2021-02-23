package com.unity.ProductsClient;

public class CameraProductInformationItems {
    private int thumbnail;
    private String title;

    public CameraProductInformationItems(int thumbnail, String title) {
        this.thumbnail = thumbnail;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getThumbnail() {
        return thumbnail;
    }
}
