package com.unity.ProductsClient;

public class TensionControlProductInformationItems {
    private final int thumbnail;
    private final String title;

    public TensionControlProductInformationItems(int thumbnail, String title) {
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
