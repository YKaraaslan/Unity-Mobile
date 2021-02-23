package com.unity;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MessageBackgroundService extends IntentService {

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public MessageBackgroundService() {
        super("MessageBackgroundService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        CollectionReference collectionReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessagesToRead");

        collectionReference.addSnapshotListener((value, error) -> {
            if (value != null){
                for (DocumentSnapshot documentSnapshot : value.getDocuments()){
                    collectionReference.document(documentSnapshot.getId()).update("received", true);
                }
            }
        });
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent intent = new Intent("com.android.ServiceStopped");
        sendBroadcast(intent);
    }
}
