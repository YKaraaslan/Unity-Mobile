package com.unity;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("StaticFieldLeak")
public class Notifications {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String token, key, title, message;
    private final String URL = "https://fcm.googleapis.com/fcm/send";

    public void sendNotification(String id, Context context, String titleReceived, String messageReceived) {
        title = titleReceived;
        message = messageReceived;
        db.collection("Tokens").document(id).get()
                .addOnSuccessListener(documentSnapshot -> token = documentSnapshot.getString("token"))
                .addOnCompleteListener(task -> getKey(context));
    }

    private void getKey(Context context) {
        db.collection("Keys").document("1").get()
                .addOnSuccessListener(documentSnapshot -> key = documentSnapshot.getString("key"))
                .addOnCompleteListener(task -> send(token, context));
    }

    private void send(String token, Context context) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/tokens/" + token);
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", title);
            notificationObject.put("body", message);
            jsonObject.put("notification", notificationObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                    response -> {
                    }, error -> {
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + key);
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
