package com.unity.HomePageClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;
import com.unity.AssignmentNotesAdd;
import com.unity.AssignmentNotesItems;
import com.unity.Notifications;
import com.unity.NotificationsForFinance;
import com.unity.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HomeFinanceNotesAdd extends AppCompatActivity {
    Toolbar toolbar;
    TextInputEditText note;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    int max_id = 0;
    Button note_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_finance_notes_add);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        note = findViewById(R.id.note);
        note_button = findViewById(R.id.note_button);

        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("NotesFinance")
                .orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                max_id = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { max_id = 1;}
        });

        if (getIntent().getBooleanExtra("update_situation", false)){
            note.setText(getIntent().getStringExtra("update_note"));
            toolbar.setTitle("Not Güncelle");
            note_button.setText("Not Güncelle");
        }
    }

    public void CreateNote(View view) {
        if (Objects.requireNonNull(note.getText()).toString().trim().equals("")){
            Toast.makeText(this, "Boş not oluşturamazsınız.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isConnected()){
            progressDialog.dismiss();
            Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getIntent().getBooleanExtra("update_situation", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.are_you_sure_to_update).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("update_document_id", 0)))
                        .collection("NotesFinance").document(String.valueOf(getIntent().getIntExtra("update_id", 0)));

                Log.d("TAG", String.valueOf(getIntent().getIntExtra("update_document_id", 0)));

                documentReference.update("note", note.getText().toString());
                documentReference.update("updated", true);
                documentReference.update("dateProcess", date);
                documentReference.update("timeProcess", time).addOnCompleteListener(task -> {
                    sendNotifications();
                    setResult(RESULT_OK);
                    Toast.makeText(HomeFinanceNotesAdd.this, "Notunuz Güncellendi", Toast.LENGTH_SHORT).show();
                    finish();
                });

            }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
            builder.create().show();
        }
        else{
            progressDialog = new ProgressDialog(HomeFinanceNotesAdd.this);
            progressDialog.setTitle(getString(R.string.adding_notes));
            progressDialog.setTitle(getString(R.string.please_wait));
            progressDialog.show();

            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            AssignmentNotesItems item = new AssignmentNotesItems(max_id, sharedPreferences.getInt("id", 0), sharedPreferences.getString("name", ""), sharedPreferences.getString("position", ""),
                    Objects.requireNonNull(note.getText()).toString().trim(), date, time, false, false, "", "");

            db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("NotesFinance").document(String.valueOf(max_id))
                    .set(item).addOnCompleteListener(task -> {
                sendNotifications();
                Toast.makeText(HomeFinanceNotesAdd.this, "Not eklendi!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                progressDialog.dismiss();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Not eklenemedi", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        }
    }

    private void sendNotifications() {
        String titleString = getIntent().getStringExtra("title");
        String message = sharedPreferences.getString("name", "") + ": " + Objects.requireNonNull(note.getText()).toString();
        NotificationsForFinance notifications = new NotificationsForFinance();
        notifications.sendNotification(this, titleString, message);
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}