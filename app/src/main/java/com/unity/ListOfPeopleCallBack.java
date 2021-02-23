package com.unity;

import android.widget.ImageView;
import android.widget.TextView;

public interface ListOfPeopleCallBack {
    void onItemClick(int position, int id, String name);
}
