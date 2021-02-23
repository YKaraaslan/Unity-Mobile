package com.unity;

import android.widget.ImageView;
import android.widget.TextView;

public interface HomeMyServicesOursCompletedCallBack {
    void onItemClick(int position, ImageView imageContainer, TextView name, TextView description, TextView time);
}
