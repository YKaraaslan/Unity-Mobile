package com.unity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;
    TextView version;
    EditText userName, password;
    ProgressDialog progressDialog;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        version = findViewById(R.id.version);
        userName = findViewById(R.id.username);
        password = findViewById(R.id.password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();
        init();
    }

    private void init() {
        version.setText(getVersionName(getApplicationContext()));
    }

    public void SignUp(View view) {
        startActivity(new Intent(this, SignUp.class));
    }

    @SuppressLint("StaticFieldLeak")
    public void LogIn(View view) {
        String userID = String.valueOf(userName.getText()).trim();
        String userPassword = String.valueOf(password.getText()).trim();

        if (userID.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(MainActivity.this, getString(R.string.blank), Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isNetworkAvailable()){
            Toast.makeText(this, getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(getString(R.string.signing_in));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if (Objects.equals(document.getString("username"), userID) && Objects.equals(document.getString("password"), userPassword)){
                        sharedEditor.putBoolean("signed", true);
                        sharedEditor.putString("name", document.getString("name"));
                        sharedEditor.putInt("authorization", Integer.parseInt(Objects.requireNonNull(document.get("authorization")).toString()));
                        sharedEditor.putInt("id", Integer.parseInt(Objects.requireNonNull(document.get("id")).toString()));
                        sharedEditor.putString("mail", document.getString("mail"));
                        sharedEditor.putString("password", document.getString("password"));
                        sharedEditor.putString("phone", document.getString("phone"));
                        sharedEditor.putString("username", document.getString("username"));
                        sharedEditor.putString("position", document.getString("position"));
                        sharedEditor.putBoolean("worker", Objects.requireNonNull(document.getBoolean("worker")));

                        sharedEditor.apply();

                        setAuthorizations();
                        return;
                    }
                }
                Toast.makeText(MainActivity.this, "Kullanıcı Adı veya Şifre Yanlış", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Bağlantı sağlanamadı", Toast.LENGTH_SHORT).show();
        });
    }

    private void setAuthorizations() {
        db.collection("Authorizations").whereEqualTo("authorization_number", sharedPreferences.getInt("authorization", 3)).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    sharedEditor.putBoolean("assignments_all", Objects.requireNonNull(document.getBoolean("assignments_all")));
                    sharedEditor.putBoolean("assignment_create", Objects.requireNonNull(document.getBoolean("assignment_create")));
                    sharedEditor.putBoolean("user_on_hold", Objects.requireNonNull(document.getBoolean("user_on_hold")));
                    sharedEditor.putBoolean("user_on_hold_buttons", Objects.requireNonNull(document.getBoolean("user_on_hold_buttons")));
                    sharedEditor.putBoolean("finance", Objects.requireNonNull(document.getBoolean("finance")));
                    sharedEditor.apply();
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Hoş Geldiniz!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, HomePage.class));
                    finish();
                });
    }

    public void PasswordForgotten(View view) {
        startActivity(new Intent(this, PasswordForgotten.class));
    }

    public void Facebook(View view) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("fb://page/358092457942200")));
        } catch (Exception e) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.facebook.com/unityotomasyonofficial")));
        }
    }

    public void Instagram(View view) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("instagram://11446755102")));
        } catch (Exception e) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://instagram.com/unity.otomasyon")));
        }
    }

    public void Twitter(View view) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("twitter://882560099197345792")));
        } catch (Exception e) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://twitter.com/unityotomasyon")));
        }
    }

    public void Web(View view) {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.unityotomasyon.com.tr")));
    }

    public void YouTube(View view) {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.youtube.com/channel/UCkGAeeXojw8ExlUiMwWMFYg")));
    }

    public void LinkedIn(View view) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("twitter://882560099197345792")));
        } catch (Exception e) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://tr.linkedin.com/company/unity-otomasyon")));
        }
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

    public String getVersionName(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}