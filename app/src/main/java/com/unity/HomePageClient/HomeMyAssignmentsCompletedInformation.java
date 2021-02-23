package com.unity.HomePageClient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.r0adkll.slidr.Slidr;
import com.unity.AssignmentNotes;
import com.unity.AssignmentsDirectAdapter;
import com.unity.AssignmentsDirectCallBack;
import com.unity.AssignmentsDirectItems;
import com.unity.AssignmentsImageViewer;
import com.unity.AssignmentsPDFViewer;
import com.unity.HomePage;
import com.unity.Notifications;
import com.unity.R;
import com.unity.WorkersInformation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeMyAssignmentsCompletedInformation extends AppCompatActivity implements AssignmentsDirectCallBack {
    Toolbar toolbar;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch guarantee, service, bill;
    Intent intent;
    TextView created_by, responsible, company, assignment_type, date_created, date_finished, title, notesAmount, date_due;
    TextInputEditText explanation_create, explanation_finish;
    ImageView image;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    AssignmentsDirectAdapter adapter;
    List<AssignmentsDirectItems> workersList = new ArrayList<>();
    RecyclerView recyclerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ConstraintLayout imageLayout, pdfLayout;
    ProgressBar progressBar;
    MaterialCardView notes;
    SharedPreferences sharedPreferences;
    Button button_reactivate, button_move, button_financially_finish;

    ConstraintLayout voice_layout;
    TextInputLayout explanationLayout;
    MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_my_assignments_completed_information);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        intent = getIntent();
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
        button_reactivate = findViewById(R.id.button_reactivate);
        notesAmount = findViewById(R.id.notesAmount);
        button_move = findViewById(R.id.button_move);
        button_financially_finish = findViewById(R.id.button_financially_finish);
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

        guarantee.setChecked(intent.getBooleanExtra("guarantee", false));
        service.setChecked(intent.getBooleanExtra("service_price", false));
        bill.setChecked(intent.getBooleanExtra("bill", false));

        String dateCreated = intent.getStringExtra("date") + " " + intent.getStringExtra("time");
        String dateFinished = intent.getStringExtra("finishDate") + " " + intent.getStringExtra("finishTime");

        title.setText(intent.getStringExtra("title"));
        created_by.setText(intent.getStringExtra("createdBy"));
        responsible.setText(intent.getStringExtra("inCharge"));
        company.setText(intent.getStringExtra("company"));
        assignment_type.setText(intent.getStringExtra("assignmentType"));
        date_created.setText(dateCreated);
        date_finished.setText(dateFinished);
        explanation_create.setText(intent.getStringExtra("note"));
        explanation_finish.setText(intent.getStringExtra("finishNote"));

        String dateDue = intent.getStringExtra("due_date") + " " + intent.getStringExtra("due_time");
        date_due.setText(dateDue);

        if (intent.getBooleanExtra("photo", false)){
            imageLayout.setVisibility(View.VISIBLE);
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(String.valueOf(intent.getIntExtra("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(image);
            }).addOnCompleteListener(task -> progressBar.setVisibility(View.GONE)).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
        }
        else if (intent.getBooleanExtra("pdf", false)){
            pdfLayout.setVisibility(View.VISIBLE);
        }

        db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0))).collection("Assigned")
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

        CollectionReference collectionReference;

        if (getIntent().getBooleanExtra("finance", false)){
            collectionReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("NotesFinance");
        }
        else{
            collectionReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes");
        }

        AtomicInteger counter = new AtomicInteger();
        collectionReference.whereEqualTo("deleted", false).addSnapshotListener((value, error) -> {
                    if (value != null){
                        counter.set(0);
                        for (DocumentSnapshot ignored : value.getDocuments()){
                            counter.getAndIncrement();
                        }
                        String lastAmount = "Görev Notları (" + counter.toString() + ")";
                        notesAmount.setText(lastAmount);
                    }
                });

        image.setOnClickListener(view -> {
            Intent intents = new Intent(HomeMyAssignmentsCompletedInformation.this, AssignmentsImageViewer.class);
            intents.putExtra("id", getIntent().getIntExtra("id", 0));
            startActivity(intents);
        });

        pdfLayout.setOnClickListener(view -> {
            Intent intents = new Intent(HomeMyAssignmentsCompletedInformation.this, AssignmentsPDFViewer.class);
            intents.putExtra("id", intent.getIntExtra("id", 0));
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

        notes.setOnClickListener(view -> {
            openNotes();
        });

        if (getIntent().getBooleanExtra("finance", false)){
            button_reactivate.setVisibility(View.GONE);
            button_move.setVisibility(View.GONE);
            button_financially_finish.setVisibility(View.VISIBLE);
        }
        else{
            button_reactivate.setVisibility(View.VISIBLE);
            button_move.setVisibility(View.VISIBLE);
            button_financially_finish.setVisibility(View.GONE);
        }

        button_move.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.are_you_sure_to_finish).setMessage(R.string.assignment_moved_can_return).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {

                DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)));
                CollectionReference colrefAssigned = documentReference.collection("Assigned");

                colrefAssigned.whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot docUsersAssigned : queryDocumentSnapshots){
                        DocumentReference userDoc = db.collection("Users").document(String.valueOf(docUsersAssigned.getLong("userID").intValue()))
                                .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));

                        userDoc.update("status", "completed");
                    }
                });

                documentReference.update("status", "completed").addOnCompleteListener(task -> {
                    db.collection("Users").document(String.valueOf(getIntent().getIntExtra("createdByID", 0)))
                            .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).update("status", "completed")
                            .addOnCompleteListener(task1 -> {
                                setResult(RESULT_OK);
                                Toast.makeText(HomeMyAssignmentsCompletedInformation.this, "Görev kaldırıldı.", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
            }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
            builder.create().show();
        });

        button_financially_finish.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeMyAssignmentsCompletedInformation.this);
            builder.setMessage(R.string.are_you_sure_to_finish).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                FinanciallyFinish();
            }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
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

    private void FinanciallyFinish() {
        DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)));
        documentReference.update("financial_situation", "passive").addOnCompleteListener(task -> {
            setResult(RESULT_OK);

            Intent intent = new Intent(getApplicationContext(), HomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Toast.makeText(HomeMyAssignmentsCompletedInformation.this, "Finansal Görev kaldırıldı.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void openNotes() {
        Intent intentForActivity;
        if (getIntent().getBooleanExtra("finance", false)){
            intentForActivity = new Intent(this, HomeFinanceNotes.class);
        }
        else{
            intentForActivity = new Intent(this, AssignmentNotes.class);
        }

        intentForActivity.putExtra("id", getIntent().getIntExtra("id", 0));
        intentForActivity.putExtra("title", getIntent().getStringExtra("title"));
        intentForActivity.putExtra("createdByID", getIntent().getIntExtra("createdByID", 0));
        intentForActivity.putExtra("finished", false);

        startActivityForResult(intentForActivity, 1);
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentsDirectAdapter(this, workersList, this);
        recyclerView.setAdapter(adapter);
    }

    public void ReturnBackToActive(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure_to_reactivate).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {

            DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)));
            CollectionReference collectionReference = documentReference.collection("Assigned");

            collectionReference.whereEqualTo("deleted", false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot docUsersAssigned : queryDocumentSnapshots){
                        DocumentReference userDoc = db.collection("Users").document(String.valueOf(docUsersAssigned.getLong("userID").intValue()))
                                .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));

                        userDoc.update("status", "active");
                    }
                }
            });

            documentReference.update("status", "active")
                    .addOnSuccessListener(aVoid -> sendNotifications()).addOnCompleteListener(task -> {
                db.collection("Users").document(String.valueOf(getIntent().getIntExtra("createdByID", 0)))
                        .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).update("status", "active");
            });
        }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
        builder.create().show();
    }

    private void sendNotifications() {
        db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)))
                .collection("Assigned").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                if(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue() != sharedPreferences.getInt("id", 0) && !Objects.requireNonNull(documentSnapshot.getBoolean("deleted")))
                    notificationSender(sharedPreferences.getString("name", ""), String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue()));
            }
            notificationSender(sharedPreferences.getString("name", ""), String.valueOf(intent.getIntExtra("createdByID", 0)));
        }).addOnCompleteListener(task -> {
            setResult(RESULT_OK);
            Toast.makeText(this, "Görev Aktifleştirildi.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void notificationSender(String name, String receivedID) {
        String title = "Dahil olduğunuz görev tekrardan aktifeştirildi";
        String message = "GÖREV: " + getIntent().getStringExtra("title") + "\nAKTİFLEŞTİREN KİŞİ: " + name;
        Notifications notifications = new Notifications();
        notifications.sendNotification(receivedID, this, title, message);
    }

    @Override
    public void onItemClick(int position) {
        if (workersList.get(position).getId() != sharedPreferences.getInt("id", 0)){
            Intent intent = new Intent(this, WorkersInformation.class);
            intent.putExtra("name", workersList.get(position).getName());
            intent.putExtra("id", workersList.get(position).getId());
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(int position, int assignedID) {

    }

    public void VoiceListener(View view) {
        player.start();
    }
}