package com.unity.HomePageClient;

import android.widget.ImageView;
import android.widget.TextView;

public interface HomeMyOffersCallBack {
    void onItemClick(int position, ImageView imageContainer, TextView name, TextView description, TextView time);
}
