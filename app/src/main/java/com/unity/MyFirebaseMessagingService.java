package com.unity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unity.HomePageClient.CurrentActivity;

import java.util.Objects;
import java.util.Random;

@SuppressLint("Registered")
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String title, message, name, userID;
    public static  final String CHANNEL_1_ID = "channel1";
    SharedPreferences sharedPreferences;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("signed", false)){
            return;
        }

        message = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
        title = remoteMessage.getNotification().getTitle();
        userID = remoteMessage.getData().get("id");
        name = remoteMessage.getData().get("name");

        if (CurrentActivity.getCurrectActivity().equalsIgnoreCase("FragmentContact")){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "App";
            String description = "App";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.canShowBadge();

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(message)
                /*.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))*/
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setColorized(true)
                .setAutoCancel(true)
                .setVibrate(new long[] { 1000, 1000 })
                .setLights(Color.RED, 1000, 1000);

        if (userID != null && Objects.requireNonNull(remoteMessage.getNotification().getClickAction()).equalsIgnoreCase("Message")){
            Intent intent = new Intent(getApplicationContext(), Message.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("userID", Integer.parseInt(userID));
            intent.putExtra("userName", title);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(Message.class);
            stackBuilder.addNextIntent(intent);

            PendingIntent conPendingIntent = stackBuilder.getPendingIntent(0,  PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(conPendingIntent);
        }

        final int random = new Random().nextInt(300);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(random, builder.build());
    }
}