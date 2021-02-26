package com.unity;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DateFormatterForMessage {
    public String format(String date) {
        String convTime = null;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date past = inputFormat.parse(date);
            Date now = new Date();
            long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - Objects.requireNonNull(past).getTime());

            String[] timeReceived = date.split(" ")[1].split(":");
            String time = timeReceived[0] + ":" + timeReceived[1];

            if (hours < 12) {
                convTime = "Son Görülme: " + time;
            } else {
                convTime = "Son Görülme: " + date;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convTime;
    }
}
