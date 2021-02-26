package com.unity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AssignmentNotesAdd extends AppCompatActivity {
    Toolbar toolbar;
    TextInputEditText note;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    int max_id = 0;
    Button note_button;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigment_notes_add);
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

        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes")
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
        if(getIntent().getBooleanExtra("update_situation", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.are_you_sure_to_update).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("update_document_id", 0)))
                        .collection("Notes").document(String.valueOf(getIntent().getIntExtra("update_id", 0)));

                documentReference.update("note", note.getText().toString());
                documentReference.update("updated", true);
                documentReference.update("dateProcess", date);
                documentReference.update("timeProcess", time).addOnCompleteListener(task -> sendNotificationsForUpdate());

            }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
            builder.create().show();
        }
        else{
            progressDialog = new ProgressDialog(AssignmentNotesAdd.this);
            progressDialog.setTitle(getString(R.string.adding_notes));
            progressDialog.setTitle(getString(R.string.please_wait));
            progressDialog.show();

            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            AssignmentNotesItems item = new AssignmentNotesItems(max_id, sharedPreferences.getInt("id", 0), sharedPreferences.getString("name", ""), sharedPreferences.getString("position", ""),
                    Objects.requireNonNull(note.getText()).toString().trim(), date, time, false, false, "", "");

            db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes").document(String.valueOf(max_id))
                    .set(item).addOnCompleteListener(task -> sendNotifications());
        }
    }

    private void sendNotificationsForUpdate() {
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("update_document_id", 0))).collection("Assigned")
                .whereEqualTo("deleted", false)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                if (Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue() != sharedPreferences.getInt("id", 0)){
                    notificationSender(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue()));
                }
            }

            if (getIntent().getIntExtra("createdByID", 0) != sharedPreferences.getInt("id", 0)){
                notificationSender(String.valueOf(getIntent().getIntExtra("createdByID", 0)));
            }
        }).addOnCompleteListener(task -> {
            Toast.makeText(AssignmentNotesAdd.this, "Notunuz Güncellendi", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void sendNotifications() {
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Assigned")
                .whereEqualTo("deleted", false)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                if (Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue() != sharedPreferences.getInt("id", 0)){
                    notificationSender(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue()));
                }
            }

            if (getIntent().getIntExtra("createdByID", 0) != sharedPreferences.getInt("id", 0)){
                notificationSender(String.valueOf(getIntent().getIntExtra("createdByID", 0)));
            }
        }).addOnCompleteListener(task -> {
            Toast.makeText(AssignmentNotesAdd.this, "Not eklendi!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void notificationSender(String receivedID) {
        String titleString = getIntent().getStringExtra("title");
        String message = sharedPreferences.getString("name", "") + ": " + Objects.requireNonNull(note.getText()).toString();
        Notifications notifications = new Notifications();
        notifications.sendNotification(receivedID, this, titleString, message);
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}