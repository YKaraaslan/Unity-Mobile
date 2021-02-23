package com.unity.HomePageClient;

import android.widget.ImageView;
import android.widget.TextView;

public interface HomeMyServicesActiveCallBack {
    void onItemClick(int position, ImageView imageContainer, TextView name, TextView description, TextView time);
}
