package com.unity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {
    ImageView imageView;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageView = findViewById(R.id.splash);

        try {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        init();
    }

    private void init() {
        imageView.setImageResource(R.drawable.unity);
        int delay = 2000;
        new Handler().postDelayed(() -> {
            sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
            if (sharedPreferences.getBoolean("signed", false)){
                startActivity(new Intent(SplashScreen.this, HomePage.class));
            }
            else {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
            }
            finish();
        }, delay);
    }
}