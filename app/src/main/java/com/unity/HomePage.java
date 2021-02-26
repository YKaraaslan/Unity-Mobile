package com.unity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HomePage extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    String selectedFragmentString, previouslySelectedFragmentString;
    String token;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        CollectionReference collectionReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessagesToRead");
        collectionReference.addSnapshotListener((value, error)
                -> {
            if (value != null) {
                int counter = value.getDocuments().size();
                if (counter > 0) {
                    bottomNavigationView.getOrCreateBadge(R.id.nav_contact).setVisible(true);
                    bottomNavigationView.getOrCreateBadge(R.id.nav_contact).setNumber(counter);
                } else {
                    bottomNavigationView.getOrCreateBadge(R.id.nav_contact).setVisible(false);
                }
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentHome()).commit();
        previouslySelectedFragmentString = "Home";
        updateToken();

        if (sharedPreferences.getInt("authorization", 3) == 4) {
            subscribeToTopic();
        }

        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (Exception ignored) {
        }

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        checkUpdate();

        startService(new Intent(this, MessageBackgroundService.class));
    }

    private void checkUpdate() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String version = mFirebaseRemoteConfig.getString("version");

                        if (!getVersionName(HomePage.this).equals(version)) {
                            startActivity(new Intent(HomePage.this, Update.class));
                            finish();
                        }
                    }
                });
    }

    public String getVersionName(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("finance")
                .addOnCompleteListener(task -> Log.d("HOMEPAGE", "SUBSCRIBED"));
    }

    private void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token = task.getResult();
                        DocumentReference document = db.collection("Tokens").document(String.valueOf(sharedPreferences.getInt("id", 0)));
                        document.update("token", token);
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.nav_home:
                selectedFragment = new FragmentHome();
                selectedFragmentString = "Home";
                break;
            case R.id.nav_products:
                Toast.makeText(this, "Sayfa henüz aktif değildir.", Toast.LENGTH_SHORT).show();
                /*selectedFragment = new FragmentProducts();
                selectedFragmentString = "Products";*/
                break;
            case R.id.nav_contact:
                selectedFragment = new FragmentContact();
                selectedFragmentString = "Contact";
                break;
            case R.id.nav_workers:
                selectedFragment = new FragmentWorkers();
                selectedFragmentString = "Workers";
                break;
            case R.id.nav_profile:
                selectedFragment = new FragmentProfile();
                selectedFragmentString = "Profile";
                break;
        }
        if (selectedFragment != null && !selectedFragmentString.equalsIgnoreCase(previouslySelectedFragmentString)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
            previouslySelectedFragmentString = selectedFragmentString;
        }
        return true;
    };


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.clickOneMoreTimeToExit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).update("online", "Online");
        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).update("message_situation", "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        final String formattedDate = date + " " + time;

        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).update("online", formattedDate);
    }
}