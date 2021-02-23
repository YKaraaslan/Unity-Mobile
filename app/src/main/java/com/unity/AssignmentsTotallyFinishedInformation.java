package com.unity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.r0adkll.slidr.Slidr;
import com.unity.HomePageClient.HomeFinanceNotes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AssignmentsTotallyFinishedInformation extends AppCompatActivity {
    Toolbar toolbar;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch guarantee, service, bill;
    TextView created_by, responsible, company, assignment_type, date_created, date_finished, title, notesAmount, notesFinanceAmount, date_due;
    TextInputEditText explanation_create, explanation_finish;
    ImageView image;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    AssignmentsDirectAdapter adapter;
    List<AssignmentsDirectItems> workersList = new ArrayList<>();
    RecyclerView recyclerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ConstraintLayout imageLayout, pdfLayout;
    ProgressBar progressBar;
    MaterialCardView notes, notesFinance;
    SharedPreferences sharedPreferences;
    Button button_reactivate;
    ConstraintLayout voice_layout;
    TextInputLayout explanationLayout;
    MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments_totally_finished_information);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        created_by = findViewById(R.id.created_by);
        responsible = findViewById(R.id.responsible);
        company = findViewById(R.id.company);
        assignment_type = findViewById(R.id.assignment_type);
        date_created = findViewById(R.id.date_created);
        date_finished = findViewById(R.id.date_finished);
        explanation_create = findViewById(R.id.explanation_create);
        explanation_finish = findViewById(R.id.explanation_finish);
        image = findViewById(R.id.image);
        imageLayout = findViewById(R.id.image_layout);
        pdfLayout = findViewById(R.id.pdf_layout);
        progressBar = findViewById(R.id.progressBar);
        title = findViewById(R.id.title);
        notes = findViewById(R.id.notes);
        notesFinance = findViewById(R.id.notesFinance);
        button_reactivate = findViewById(R.id.button_reactivate);
        notesAmount = findViewById(R.id.notesAmount);
        notesFinanceAmount = findViewById(R.id.notesFinanceAmount);
        date_due = findViewById(R.id.date_due);
        explanationLayout = findViewById(R.id.outlinedTextField);
        voice_layout = findViewById(R.id.voice_layout);

        recyclerView = findViewById(R.id.recycler_view);
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        setupRecyclerView();

        guarantee = findViewById(R.id.guarantee);
        service = findViewById(R.id.service);
        bill = findViewById(R.id.bill);

        guarantee.setClickable(false);
        service.setClickable(false);
        bill.setClickable(false);

        guarantee.setChecked(getIntent().getBooleanExtra("guarantee", false));
        service.setChecked(getIntent().getBooleanExtra("service_price", false));
        bill.setChecked(getIntent().getBooleanExtra("bill", false));

        String dateCreated = getIntent().getStringExtra("date") + " " + getIntent().getStringExtra("time");
        String dateFinished = getIntent().getStringExtra("finishDate") + " " + getIntent().getStringExtra("finishTime");

        title.setText(getIntent().getStringExtra("title"));
        created_by.setText(getIntent().getStringExtra("createdBy"));
        responsible.setText(getIntent().getStringExtra("inCharge"));
        company.setText(getIntent().getStringExtra("company"));
        assignment_type.setText(getIntent().getStringExtra("assignmentType"));
        date_created.setText(dateCreated);
        date_finished.setText(dateFinished);
        explanation_create.setText(getIntent().getStringExtra("note"));
        explanation_finish.setText(getIntent().getStringExtra("finishNote"));

        String dateDue = getIntent().getStringExtra("due_date") + " " + getIntent().getStringExtra("due_time");
        date_due.setText(dateDue);

        if (getIntent().getBooleanExtra("photo", false)){
            imageLayout.setVisibility(View.VISIBLE);
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(String.valueOf(getIntent().getIntExtra("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(image)).addOnCompleteListener(task -> progressBar.setVisibility(View.GONE)).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
        }
        else if (getIntent().getBooleanExtra("pdf", false)){
            pdfLayout.setVisibility(View.VISIBLE);
        }

        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Assigned")
                .orderBy("id", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if (!Objects.requireNonNull(document.getBoolean("deleted"))){
                        workersList.add(new AssignmentsDirectItems(Objects.requireNonNull(document.getLong("userID")).intValue(), Objects.requireNonNull(document.getLong("id")).intValue(),
                                document.getString("username"), document.getString("seen_date"), document.getString("seen_time"), Objects.requireNonNull(document.getBoolean("seen_situation"))));

                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        AtomicInteger counter = new AtomicInteger();
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes").whereEqualTo("deleted", false)
                .addSnapshotListener((value, error) -> {
                    if (value != null){
                        counter.set(0);
                        for (DocumentSnapshot ignored : value.getDocuments()){
                            counter.getAndIncrement();
                        }
                        String lastAmount = "Görev Notları (" + counter.toString() + ")";
                        notesAmount.setText(lastAmount);
                    }
                });

        AtomicInteger counterFinance = new AtomicInteger();
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("NotesFinance").whereEqualTo("deleted", false)
                .addSnapshotListener((value, error) -> {
                    if (value != null){
                        counterFinance.set(0);
                        for (DocumentSnapshot ignored : value.getDocuments()){
                            counterFinance.getAndIncrement();
                        }
                        String lastAmount = "Finansal Notlar (" + counterFinance.toString() + ")";
                        notesFinanceAmount.setText(lastAmount);
                    }
                });

        image.setOnClickListener(view -> {
            Intent intents = new Intent(AssignmentsTotallyFinishedInformation.this, AssignmentsImageViewer.class);
            intents.putExtra("id", getIntent().getIntExtra("id", 0));
            startActivity(intents);
        });

        pdfLayout.setOnClickListener(view -> {
            Intent intents = new Intent(AssignmentsTotallyFinishedInformation.this, AssignmentsPDFViewer.class);
            intents.putExtra("id", getIntent().getIntExtra("id", 0));
            startActivity(intents);
        });

        Query query = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Assigned").whereEqualTo("userID", sharedPreferences.getInt("id", 0));
        CollectionReference collectionReferences = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Assigned");

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                if (documentSnapshot.getBoolean("seen_situation") != null){
                    if (!Objects.requireNonNull(documentSnapshot.getBoolean("seen_situation"))){
                        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        collectionReferences.document(documentSnapshot.getId()).update("seen_date", date);
                        collectionReferences.document(documentSnapshot.getId()).update("seen_time", time);
                        collectionReferences.document(documentSnapshot.getId()).update("seen_situation", true);
                    }
                }
            }
        });

        notes.setOnClickListener(view -> openNotes());

        notesFinance.setOnClickListener(view -> openNotesFinance());

        if (getIntent().getBooleanExtra("finance", false)){
            button_reactivate.setVisibility(View.GONE);
        }

        button_reactivate.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.are_you_sure_to_move_assignemnts).setMessage(R.string.assignment_moved_can_return).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> returnBackToAssignments()).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
            builder.create().show();
        });

        if (Objects.equals(getIntent().getStringExtra("note"), "") || getIntent().getStringExtra("note") == null){
            explanationLayout.setVisibility(View.GONE);
        }

        StorageReference storageReference = storage.getReference();
        storageReference.child("AssignmentsAudio").child(String.valueOf(getIntent().getIntExtra("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> {
            if (uri != null){
                voice_layout.setVisibility(View.VISIBLE);
                try {
                    player.setDataSource(String.valueOf(uri));
                    player.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openNotes() {
        Intent intentForActivity = new Intent(this, AssignmentNotes.class);
        intentForActivity.putExtra("id", getIntent().getIntExtra("id", 0));
        intentForActivity.putExtra("title", getIntent().getStringExtra("title"));
        intentForActivity.putExtra("createdByID", getIntent().getIntExtra("createdByID", 0));
        intentForActivity.putExtra("finished", false);

        startActivity(intentForActivity);
    }

    private void openNotesFinance() {
        Intent intentForActivity = new Intent(this, HomeFinanceNotes.class);
        intentForActivity.putExtra("id", getIntent().getIntExtra("id", 0));
        intentForActivity.putExtra("title", getIntent().getStringExtra("title"));
        intentForActivity.putExtra("createdByID", getIntent().getIntExtra("createdByID", 0));
        intentForActivity.putExtra("finished", false);

        startActivity(intentForActivity);
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentsDirectAdapter(this, workersList, null);
        recyclerView.setAdapter(adapter);
    }

    public void returnBackToAssignments() {
        DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));
        CollectionReference collectionReference = documentReference.collection("Assigned");

        collectionReference.whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot docUsersAssigned : queryDocumentSnapshots){
                DocumentReference userDoc = db.collection("Users").document(String.valueOf(Objects.requireNonNull(docUsersAssigned.getLong("userID")).intValue()))
                        .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));

                userDoc.update("status", "passive");
            }
        });

        documentReference.update("status", "passive").addOnSuccessListener(aVoid -> db.collection("Users").document(String.valueOf(getIntent().getIntExtra("createdByID", 0)))
                .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).update("status", "passive")).addOnCompleteListener(task -> {
            Toast.makeText(AssignmentsTotallyFinishedInformation.this, "Görevlere Taşındı.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AssignmentsTotallyFinishedInformation.this, HomePage.class));
            finish();
        });
    }

    public void VoiceListener(View view) {
        player.start();
    }
}