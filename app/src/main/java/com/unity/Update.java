package com.unity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class Update extends AppCompatActivity {
    Toolbar toolbar;

    private static final String KEY_UPDATE_URL = "updateUrl";
    private static final String KEY_UPDATE_TEXT_TR = "updateTextTr";
    private static final String KEY_UPDATE_VISIT_APK = "updateVisitApk";

    boolean doubleBackToExitPressedOnce = false;
    TextView updateText;
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    ProgressBar progressBar;
    final Uri auto_download = Uri.parse(mFirebaseRemoteConfig.getString(KEY_UPDATE_URL));
    final Uri visitApk = Uri.parse(mFirebaseRemoteConfig.getString(KEY_UPDATE_VISIT_APK));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        updateText = findViewById(R.id.updateText);
        progressBar = findViewById(R.id.progressBar);

        String text = mFirebaseRemoteConfig.getString(KEY_UPDATE_TEXT_TR).replace("\\n", "\n");

        updateText.setText(text);
        progressBar.setVisibility(View.GONE);
    }

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

    public void Download(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, auto_download);
        startActivity(intent);
    }

    public void VisitAPK(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, visitApk);
        startActivity(intent);
    }
}