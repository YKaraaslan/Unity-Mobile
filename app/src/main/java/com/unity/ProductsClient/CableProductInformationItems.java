package com.unity.ProductsClient;

public class CableProductInformationItems {
    private final int thumbnail;
    private final String title;

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
