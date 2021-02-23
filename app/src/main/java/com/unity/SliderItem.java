package com.unity;

import android.graphics.Bitmap;

public class SliderItem {
    public Bitmap image;
    public String description;

    public SliderItem(Bitmap image, String description) {
        this.image = image;
        this.description = description;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }
}
