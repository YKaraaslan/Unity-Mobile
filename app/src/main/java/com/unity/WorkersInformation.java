package com.unity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.r0adkll.slidr.Slidr;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkersInformation extends AppCompatActivity {
    Toolbar toolbar;

    CircularImageView workersImage;
    TextView workers_name, workers_description, rating_text, workers_time, projects_done_text, projects_todo_text, projects_completed_text;
    TextView worker_phone, worker_mail, project_todo_linearLayout_text, project_done_linearLayout_text, project_completed_linearLayout_text;
    RatingBar rating;
    LinearLayout projects_done_layout, projects_todo_layout, linearLayout_projects;
    LinearLayout complaint_linearLayout, project_done_linearLayout, project_todo_linearLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    AtomicInteger activeCounter = new AtomicInteger();
    AtomicInteger passiveCounter = new AtomicInteger();
    AtomicInteger completedCounter = new AtomicInteger();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    SharedPreferences sharedPreferences;
    LinearLayout sendMessage, assignPerson, project_completed_linearLayout, projects_completed_layout;
    View view, view2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workers_information);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolbar.setTitle(getIntent().getStringExtra("name"));
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        workersImage = findViewById(R.id.workersImage);
        workers_name = findViewById(R.id.workers_name);
        workers_description = findViewById(R.id.workers_description);
        rating_text = findViewById(R.id.rating_text);
        workers_time = findViewById(R.id.workers_time);
        projects_done_text = findViewById(R.id.projects_done_text);
        projects_todo_text = findViewById(R.id.projects_todo_text);
        worker_phone = findViewById(R.id.worker_phone);
        worker_mail = findViewById(R.id.worker_mail);
        project_todo_linearLayout_text = findViewById(R.id.project_todo_linearLayout_text);
        project_done_linearLayout_text = findViewById(R.id.project_done_linearLayout_text);
        project_completed_linearLayout = findViewById(R.id.project_completed_linearLayout);
        projects_completed_layout = findViewById(R.id.projects_completed_layout);

        rating = findViewById(R.id.rating);
        projects_completed_text = findViewById(R.id.projects_completed_text);
        projects_done_layout = findViewById(R.id.projects_done_layout);
        projects_todo_layout = findViewById(R.id.projects_todo_layout);
        complaint_linearLayout = findViewById(R.id.complaint_linearLayout);
        project_done_linearLayout = findViewById(R.id.project_done_linearLayout);
        project_todo_linearLayout = findViewById(R.id.project_todo_linearLayout);
        linearLayout_projects = findViewById(R.id.linearLayout_projects);
        project_completed_linearLayout_text = findViewById(R.id.project_completed_linearLayout_text);

        sendMessage = findViewById(R.id.sendMessage);
        assignPerson = findViewById(R.id.assignPerson);
        view = findViewById(R.id.view);
        view2 = findViewById(R.id.view2);

        storageReference.child("Users").child(String.valueOf(getIntent().getIntExtra("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(this)
                .load(uri.toString())
                .centerCrop()
                .into(workersImage));

        projects_done_layout.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkersInformation.this);
            alertDialogBuilder.setMessage("Tamamlanan Görev Sayısı: " + passiveCounter);
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {

            }).show();
        });
        projects_todo_layout.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkersInformation.this);
            alertDialogBuilder.setMessage("Aktif Görev Sayısı: " + activeCounter);
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {

            }).show();
        });
        projects_completed_layout.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkersInformation.this);
            alertDialogBuilder.setMessage("Biten Görev Sayısı: " + completedCounter);
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {

            }).show();
        });
        complaint_linearLayout.setOnClickListener(view -> {

        });
        project_done_linearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(WorkersInformation.this, WorkersInformationAssignments.class);
            intent.putExtra("id", getIntent().getIntExtra("id", 0));
            intent.putExtra("status", "passive");
            startActivity(intent);
        });
        project_todo_linearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(WorkersInformation.this, WorkersInformationAssignments.class);
            intent.putExtra("id", getIntent().getIntExtra("id", 0));
            intent.putExtra("status", "active");
            startActivity(intent);
        });

        project_completed_linearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(WorkersInformation.this, WorkersInformationAssignments.class);
            intent.putExtra("id", getIntent().getIntExtra("id", 0));
            intent.putExtra("status", "completed");
            startActivity(intent);
        });

        sendMessage.setOnClickListener(view -> {
            Intent intent = new Intent(WorkersInformation.this, Message.class);
            intent.putExtra("userName", getIntent().getStringExtra("name"));
            intent.putExtra("userID", getIntent().getIntExtra("id", 0));
            startActivity(intent);
            finish();
        });

        assignPerson.setOnClickListener(view -> {
            Intent intent = new Intent(WorkersInformation.this, WorkersInformationAssignmentCreate.class);
            intent.putExtra("userName", getIntent().getStringExtra("name"));
            intent.putExtra("userID", getIntent().getIntExtra("id", 0));
            startActivity(intent);
        });

        if (sharedPreferences.getBoolean("assignment_create", false)){
            assignPerson.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }

        if (sharedPreferences.getBoolean("assignments_all", false)){
            linearLayout_projects.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
        }

        project_completed_linearLayout.setOnClickListener(view -> {

        });

        setTextViews();
    }

    @SuppressLint("SetTextI18n")
    private void setTextViews() {
        if (getIntent().getStringExtra("position") != null){
            workers_description.setText(getIntent().getStringExtra("position"));
        }
        else{
            db.collection("Users").document(String.valueOf(getIntent().getIntExtra("id", 0))).get()
                    .addOnSuccessListener(documentSnapshot -> workers_description.setText(documentSnapshot.getString("position")));
        }

        workers_name.setText(getIntent().getStringExtra("name"));

        DateFormatter dateFormatter = new DateFormatter();
        String convTime = dateFormatter.format(getIntent().getStringExtra("online"));
        workers_time.setText(convTime);

        db.collection("Assignments").orderBy("id", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    db.collection("Assignments").document(documentSnapshot.getId()).collection("Assigned")
                            .whereEqualTo("userID", getIntent().getIntExtra("id", 0))
                            .whereEqualTo("deleted", false)
                            .get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (int i = 0; i < Objects.requireNonNull(task1.getResult()).size(); i++) {
                                if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("active")){
                                    activeCounter.getAndIncrement();
                                    projects_todo_text.setText(String.valueOf(activeCounter));
                                    project_todo_linearLayout_text.setText("Aktif Görevler (" + activeCounter + ")");
                                }
                                else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("passive")){
                                    passiveCounter.getAndIncrement();
                                    projects_done_text.setText(String.valueOf(passiveCounter));
                                    project_done_linearLayout_text.setText("Tamamlanan Görevler (" + passiveCounter + ")");
                                }
                                else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("completed")){
                                    completedCounter.getAndIncrement();
                                    projects_completed_text.setText(String.valueOf(completedCounter));
                                    project_completed_linearLayout_text.setText("Biten Görevler (" + completedCounter + ")");
                                }
                            }
                        }
                    });

                    if (Objects.requireNonNull(documentSnapshot.getLong("createdByID")).intValue() == getIntent().getIntExtra("id", 0)){
                        if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("active")){
                            activeCounter.getAndIncrement();
                            projects_todo_text.setText(String.valueOf(activeCounter));
                            project_todo_linearLayout_text.setText("Aktif Görevler (" + activeCounter + ")");
                        }
                        else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("passive")){
                            passiveCounter.getAndIncrement();
                            projects_done_text.setText(String.valueOf(passiveCounter));
                            project_done_linearLayout_text.setText("Tamamlanan Görevler (" + passiveCounter + ")");
                        }
                        else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("completed")){
                            completedCounter.getAndIncrement();
                            projects_completed_text.setText(String.valueOf(completedCounter));
                            project_completed_linearLayout_text.setText("Biten Görevler (" + completedCounter + ")");
                        }
                    }
                }

            }
        });

        db.collection("Users").whereEqualTo("id", getIntent().getIntExtra("id", 0)).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                worker_phone.setText(documentSnapshot.getString("phone"));
                worker_mail.setText(documentSnapshot.getString("mail"));
            }
        });
    }
}