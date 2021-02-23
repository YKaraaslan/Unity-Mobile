package com.unity.ProductsClient;

public class CableProductInformationItems {
    private int thumbnail;
    private String title;

    public CableProductInformationItems(int thumbnail, String title) {
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
