package com.unity.HomePageClient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.r0adkll.slidr.Slidr;
import com.unity.Notifications;
import com.unity.NotificationsForFinance;
import com.unity.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeMyAssignmentsFinish extends AppCompatActivity {
    Toolbar toolbar;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch guaranteeSwitch, serviceSwitch, billSwitch;
    Intent intent;
    TextInputEditText last_note;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Integer> workers = new ArrayList<>();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_my_assignments_finish);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        intent = getIntent();
        guaranteeSwitch = findViewById(R.id.guarantee);
        serviceSwitch = findViewById(R.id.service);
        billSwitch = findViewById(R.id.bill);
        last_note = findViewById(R.id.last_note);

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        getUsers();
    }

    private void getUsers() {
        DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)));

        documentReference.collection("Assigned").orderBy("id", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    workers.add(Objects.requireNonNull(document.getLong("id")).intValue());
                }
            }
        });
    }

    public void AssignmentFinish(View view) {
        if (Objects.requireNonNull(last_note.getText()).toString().equals("")){
            Toast.makeText(this, "Aciklama ekleyiniz", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.assignment_finish_text))
                .setPositiveButton(R.string.ok, (dialogInterface, i1) -> Finish()).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();

    }

    private void Finish() {
        boolean guarantee = guaranteeSwitch.isChecked(), service = serviceSwitch.isChecked(), bill = billSwitch.isChecked();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        DocumentReference document = db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)));

        document.update("bill", bill);
        document.update("service_price", service);
        document.update("guarantee", guarantee);
        document.update("finishDate", date);
        document.update("finishTime", time);
        document.update("finishNote", Objects.requireNonNull(last_note.getText()).toString().trim());
        document.update("status", "passive");
        document.update("finishedBy", sharedPreferences.getString("name", ""));
        document.update("finishedByID", sharedPreferences.getInt("id", 0));

        document.collection("Assigned").whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()){
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    int userID = Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue();
                    DocumentReference documentReference = db.collection("Users").document(String.valueOf(userID)).collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)));

                    documentReference.update("finish_note", last_note.getText().toString().trim());
                    documentReference.update("finish_date", date);
                    documentReference.update("finish_time", time);
                    documentReference.update("status", "passive");
                }
            }

            DocumentReference docRefForCreatedBy = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("createdByID", 0))).collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));
            docRefForCreatedBy.update("finish_note", last_note.getText().toString().trim());
            docRefForCreatedBy.update("finish_date", date);
            docRefForCreatedBy.update("finish_time", time);
            docRefForCreatedBy.update("status", "passive");
        }).addOnCompleteListener(task -> {
            if (bill || service){
                document.update("financial_situation", "active");
                sendNotificationsToFinance();
            }
            sendNotifications();
        });
    }

    private void sendNotifications() {
        db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)))
                .collection("Assigned")
                .whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                if (!Objects.requireNonNull(documentSnapshot.getBoolean("deleted"))){
                    notificationSender(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue()));
                }
            }
            notificationSender(String.valueOf(intent.getIntExtra("createdByID", 0)));
        }).addOnCompleteListener(task -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    private void notificationSender(String userID) {
        String title = "Dahil olduğunuz görev sonlandırıldı";
        String message = "GÖREV: " + getIntent().getStringExtra("title") + "\nSONLANDIRAN KİŞİ: " + sharedPreferences.getString("name", "");
        Notifications notifications = new Notifications();
        notifications.sendNotification(userID, this, title, message);
    }

    private void sendNotificationsToFinance() {
        String title = "Finansal bir görev sonlandırıldı";
        String message = "GÖREV: " + getIntent().getStringExtra("title") + "\nSONLANDIRAN KİŞİ: " + sharedPreferences.getString("name", "");
        NotificationsForFinance notifications = new NotificationsForFinance();
        notifications.sendNotification(this, title, message);
    }
}