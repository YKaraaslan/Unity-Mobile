package com.unity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.r0adkll.slidr.Slidr;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class UsersOnHoldInformation extends AppCompatActivity {
    Toolbar toolbar;
    Intent intent;
    String name, mail, phone, username, password, position, date_applied, time_applied, dateTimeCombined_applied;
    int authorization, id;
    boolean online;
    TextView workers_name, datetime, worker_phone, worker_mail;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    int usersMaxID = 0, constUsersMaxID = 0, usersRejectedMaxID = 0, constUsersRejectedMaxID = 0;
    Spinner auth;
    EditText positionEditText;
    Switch worker;
    SharedPreferences sharedPreferences;
    Button confirm, deny;
    CircularImageView image;
    Bitmap bitmap;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_on_hold_information);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        workers_name = findViewById(R.id.workers_name);
        datetime = findViewById(R.id.datetime);
        worker_phone = findViewById(R.id.worker_phone);
        worker_mail = findViewById(R.id.worker_mail);
        positionEditText = findViewById(R.id.position);
        worker = findViewById(R.id.worker);
        confirm = findViewById(R.id.confirm);
        deny = findViewById(R.id.deny);
        image = findViewById(R.id.workersImage);

        intent = getIntent();

        name = intent.getStringExtra("name");
        mail = intent.getStringExtra("mail");
        phone = intent.getStringExtra("phone");
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        position = intent.getStringExtra("position");
        date_applied = intent.getStringExtra("date_applied");
        time_applied = intent.getStringExtra("time_applied");
        authorization = intent.getIntExtra("authorization", 0);
        id = intent.getIntExtra("id", 0);
        online = intent.getBooleanExtra("online", false);

        dateTimeCombined_applied = date_applied + " " + time_applied;

        workers_name.setText(name);
        datetime.setText(dateTimeCombined_applied);
        worker_phone.setText(phone);
        worker_mail.setText(mail);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.child("UsersOnHold").child(String.valueOf(id)).getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            image.setImageBitmap(bitmap);
        }).addOnFailureListener(Throwable::printStackTrace);

        db.collection("Users").orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                usersMaxID = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { usersMaxID = 1;}
        });


        db.collection("UsersRejected").orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                usersRejectedMaxID = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { usersRejectedMaxID = 1;}
        });

        auth =  findViewById(R.id.authorization);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.authorization_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        auth.setAdapter(adapter);

        if (sharedPreferences.getBoolean("user_on_hold_buttons",false)){
            confirm.setVisibility(View.VISIBLE);
            deny.setVisibility(View.VISIBLE);
        }

        image.setOnClickListener(view -> {
            Intent intents = new Intent(UsersOnHoldInformation.this, UsersImageViewer.class);
            intents.putExtra("id", id);
            intents.putExtra("document", "UsersOnHold");
            startActivity(intents);
        });
    }

    public void Confirm(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.confirm_text) + " " + name)
                .setTitle(getString(R.string.confirm_title))
                .setPositiveButton(R.string.ok, (dialogInterface, i1) -> {
                    constUsersMaxID = usersMaxID;
                    ConfirmUser();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();
    }

    private void ConfirmUser() {
        String dateNow = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String timeNow = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        position = positionEditText.getText().toString().trim();

        if (position.equals("")){
            Toast.makeText(this, "Pozisyonu doldurunuz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isConnected()){
            Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").document(String.valueOf(constUsersMaxID)).set(new UsersOnHoldItems(name, mail, phone, username, password, position, date_applied, time_applied, dateNow, timeNow,
                getAuthorization(), constUsersMaxID, worker.isChecked(), "", sharedPreferences.getString("name", ""), sharedPreferences.getInt("id", 0)))
                .addOnSuccessListener(aVoid -> {
                    String title = "Kayıt işleminiz onaylandı";
                    String message = "Giriş yaparak uygulamamızı kullabilirsiniz";
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Photo", null);

                    storageReference.child("Users").child(constUsersMaxID + ".jpg").putFile(Uri.parse(path))
                            .addOnSuccessListener(taskSnapshot -> storageReference.child("UsersOnHold").child(String.valueOf(id)).delete());

                    NotificationsForUsersToSignUp notifications = new NotificationsForUsersToSignUp();
                    notifications.sendNotification(String.valueOf(id), this, title, message);
                }).addOnCompleteListener(task -> deleteUserFromUsersOnHoldForConfirmation());
    }

    private void deleteUserFromUsersOnHoldForConfirmation() {
        db.collection("UsersOnHold").document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(aVoid -> db.collection("UsersOnHoldTokens").document(String.valueOf(id))
                        .get().addOnCompleteListener(task -> addTokentoUsers(task.getResult().getString("token"))));
    }

    private void addTokentoUsers(String token) {
        db.collection("Tokens").document(String.valueOf(constUsersMaxID))
                .set(new TokenItems(token))
        .addOnCompleteListener(task -> db.collection("UsersOnHoldTokens").document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UsersOnHoldInformation.this, "Kullanıcı Başarıyla kaydedildi!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }));
    }

    private int getAuthorization() {
        switch (auth.getSelectedItem().toString()){
            case "Admin":
                return 1;

            case "Müdür":
                return 2;

            case "Çalışan":
                return 3;

            case "Finans":
                return 4;
        }
        return 3;
    }

    public void Deny(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.deny_text) + " " + name)
                .setTitle(getString(R.string.deny_title))
                .setPositiveButton(R.string.ok, (dialogInterface, i1) -> {
                    if (!isConnected()){
                        Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    constUsersRejectedMaxID = usersRejectedMaxID;
                    DenyUser();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();
    }

    private void DenyUser() {
        String dateNow = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String timeNow = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        position = positionEditText.getText().toString();
        db.collection("UsersRejected").document(String.valueOf(constUsersRejectedMaxID)).set(new UsersOnHoldItems(name, mail, phone, username, password, position, date_applied, time_applied, dateNow, timeNow,
                getAuthorization(), constUsersRejectedMaxID, worker.isChecked(), "", sharedPreferences.getString("name", ""), sharedPreferences.getInt("id", 0))).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String title = "Kayıt işleminiz reddedildi";
                String message = "Uygulamamızdan yeni kullanıcı kaydı oluşturabilirsiniz";
                NotificationsForUsersToSignUp notifications = new NotificationsForUsersToSignUp();
                notifications.sendNotification(String.valueOf(id), this, title, message);
                deleteUserFromUsersOnHoldForDeny();
            }
        });
    }

    private void deleteUserFromUsersOnHoldForDeny() {
        storageReference.child("UsersOnHold").child(String.valueOf(id)).delete();
        db.collection("UsersOnHold").document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(aVoid -> {

                }).addOnCompleteListener(task -> deleteTokensForDeny());
    }

    private void deleteTokensForDeny() {
        db.collection("UsersOnHoldTokens").document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UsersOnHoldInformation.this, "Kullanıcı Reddedildi!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}