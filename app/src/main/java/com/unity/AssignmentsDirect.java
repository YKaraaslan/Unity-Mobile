package com.unity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.unity.HomePageClient.HomeMyAssignmentsFinish;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AssignmentsDirect extends AppCompatActivity implements AssignmentsDirectCallBack {
    Toolbar toolbar;
    TextView person, company, assignment_type, degree_of_urgency, pdfName, title, notesAmount, created_by;
    TextView date_created, date_due;
    TextInputEditText explanation;
    TextInputLayout explanationLayout;
    ImageView image;
    int id;
    boolean pdf, photo, startedListening;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Intent intentRecevied;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ConstraintLayout pdfLayout, image_layout;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    MaterialCardView notes;
    ConstraintLayout voice_layout;

    MediaPlayer player = new MediaPlayer();
    RecyclerView recyclerView;
    AssignmentsDirectAdapter adapter;
    List<AssignmentsDirectItems> workersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments_direct);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        intentRecevied = getIntent();
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        person = findViewById(R.id.person);
        company = findViewById(R.id.company);
        assignment_type = findViewById(R.id.assignment_type);
        degree_of_urgency = findViewById(R.id.degree_of_urgency);
        explanation = findViewById(R.id.explanation);
        image = findViewById(R.id.image);
        pdfLayout = findViewById(R.id.pdf_layout);
        image_layout = findViewById(R.id.image_layout);
        pdfName = findViewById(R.id.pdf_name);
        progressBar = findViewById(R.id.progressBar);
        title = findViewById(R.id.title);
        notes = findViewById(R.id.notes);
        notesAmount = findViewById(R.id.notesAmount);
        created_by = findViewById(R.id.created_by);
        date_created = findViewById(R.id.date_created);
        date_due = findViewById(R.id.date_due);
        explanationLayout = findViewById(R.id.outlinedTextField);
        voice_layout = findViewById(R.id.voice_layout);

        recyclerView = findViewById(R.id.recycler_view);

        setupRecyclerView();

        person.setText(intentRecevied.getStringExtra("inCharge"));
        company.setText(intentRecevied.getStringExtra("company"));
        assignment_type.setText(intentRecevied.getStringExtra("assignmentType"));
        degree_of_urgency.setText(intentRecevied.getStringExtra("urgency"));
        explanation.setText(intentRecevied.getStringExtra("note"));
        title.setText(intentRecevied.getStringExtra("title"));
        created_by.setText(intentRecevied.getStringExtra("createdBy"));

        String dateCreated = intentRecevied.getStringExtra("date") + " " + intentRecevied.getStringExtra("time");
        String dateDue = intentRecevied.getStringExtra("due_date") + " " + intentRecevied.getStringExtra("due_time");

        date_created.setText(dateCreated);
        date_due.setText(dateDue);

        id = intentRecevied.getIntExtra("id", 0);
        pdf = intentRecevied.getBooleanExtra("pdf", false);
        photo = intentRecevied.getBooleanExtra("photo", false);

        Query query = db.collection("Assignments").document(String.valueOf(id)).collection("Assigned").whereEqualTo("userID", sharedPreferences.getInt("id", 0));
        CollectionReference collectionReference = db.collection("Assignments").document(String.valueOf(id)).collection("Assigned");

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                if (documentSnapshot.getBoolean("seen_situation") != null){
                    if (!Objects.requireNonNull(documentSnapshot.getBoolean("seen_situation"))){
                        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        collectionReference.document(documentSnapshot.getId()).update("seen_date", date);
                        collectionReference.document(documentSnapshot.getId()).update("seen_time", time);
                        collectionReference.document(documentSnapshot.getId()).update("seen_situation", true);
                    }
                }
            }
        });

        if (pdf) {
            pdfLayout.setVisibility(View.VISIBLE);
        } else if (photo) {
            image_layout.setVisibility(View.VISIBLE);

            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(String.valueOf(id)).getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri.toString())
                        .centerCrop()
                        .into(image);
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        pdfLayout.setOnClickListener(view -> openPdf());

        db.collection("Assignments").document(String.valueOf(id)).collection("Assigned").orderBy("id", Query.Direction.ASCENDING)
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
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                    counter.getAndIncrement();
                }
                String lastAmount = notesAmount.getText() + " (" + counter.toString() + ")";
                notesAmount.setText(lastAmount);
            }
        });


        image.setOnClickListener(view -> {
            Intent intent = new Intent(this, AssignmentsImageViewer.class);
            intent.putExtra("id", intentRecevied.getIntExtra("id", 0));
            startActivity(intent);
        });

        notes.setOnClickListener(view -> {
            openNotes();
        });

        if (Objects.equals(intentRecevied.getStringExtra("note"), "")){
            explanationLayout.setVisibility(View.GONE);
        }

        StorageReference storageReference = storage.getReference();
        storageReference.child("AssignmentsAudio").child(String.valueOf(id)).getDownloadUrl().addOnSuccessListener(uri -> {
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
        Intent intent = new Intent(this, AssignmentNotes.class);

        intent.putExtra("id", intentRecevied.getIntExtra("id", 0));
        intent.putExtra("title", intentRecevied.getStringExtra("title"));
        intent.putExtra("createdByID", intentRecevied.getIntExtra("createdByID", 0));
        intent.putExtra("finished", false);

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            reloadNotes();
        }
    }

    private void reloadNotes() {
        AtomicInteger counter = new AtomicInteger();
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes")
                .whereEqualTo("deleted", false)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                    counter.getAndIncrement();
                }
                String lastAmount = "Notlar (" + counter.toString() + ")";
                notesAmount.setText(lastAmount);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getIntExtra("createdByID", 0) == sharedPreferences.getInt("id", 0)){
            getMenuInflater().inflate(R.menu.assignment_active_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit:
                Intent intent = new Intent(this, AssignmentsUpdate.class);
                intent.putExtra("companyId", getIntent().getIntExtra("companyId", 0));
                intent.putExtra("createdByID", getIntent().getIntExtra("createdByID", 0));
                intent.putExtra("id", getIntent().getIntExtra("id", 0));
                intent.putExtra("inChargeID", getIntent().getIntExtra("inChargeID", 0));
                intent.putExtra("urgency", getIntent().getStringExtra("urgency"));
                intent.putExtra("urgencyNumber", getIntent().getIntExtra("urgencyNumber", 0));
                intent.putExtra("assignmentType", getIntent().getStringExtra("assignmentType"));
                intent.putExtra("company",getIntent().getStringExtra("company"));
                intent.putExtra("createdBy", getIntent().getStringExtra("createdBy"));
                intent.putExtra("date", getIntent().getStringExtra("date"));
                intent.putExtra("inCharge", getIntent().getStringExtra("inCharge"));
                intent.putExtra("note", getIntent().getStringExtra("note"));
                intent.putExtra("status", getIntent().getStringExtra("status"));
                intent.putExtra("time", getIntent().getStringExtra("time"));
                intent.putExtra("pdf", getIntent().getBooleanExtra("pdf", false));
                intent.putExtra("photo", getIntent().getBooleanExtra("photo", false));
                intent.putExtra("title", getIntent().getStringExtra("title"));
                intent.putExtra("due_date", getIntent().getStringExtra("due_date"));
                intent.putExtra("due_time", getIntent().getStringExtra("due_time"));

                startActivity(intent);
                finish();
                break;

            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.are_you_sure_to_delete_assignment).setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Assigned").whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots){
                            String titleString = sharedPreferences.getString("name", "") + " dahil olduğunuz görevi sildi.";
                            String message = "Silinen Görev: " + getIntent().getStringExtra("title");
                            Notifications notifications = new Notifications();
                            notifications.sendNotification(String.valueOf(Objects.requireNonNull(doc.getLong("userID")).intValue()), this, titleString, message);
                        }
                    });

                    startDeleting();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> { });
                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startDeleting() {
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).get()
                .addOnSuccessListener(documentSnapshotSecond -> {
                    String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                    DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue()));
                    documentReference.update("status", "deleted");

                    documentReference.collection("DeletedBy").add(new AssignmentsDeleteItems(sharedPreferences.getString("name", ""), date, sharedPreferences.getInt("id", 0)));

                    documentReference.collection("Assigned").whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot assignedUserDocs : queryDocumentSnapshots){
                            DocumentReference documentReferenceForUser = db.collection("Users").document(String.valueOf(Objects.requireNonNull(assignedUserDocs.getLong("userID")).intValue()))
                                    .collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue()));

                            documentReferenceForUser.update("status", "deleted");
                        }
                        Toast.makeText(this, "Görev silindi.", Toast.LENGTH_SHORT).show();
                        finish();
                    });

                    db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Assignments")
                            .document(String.valueOf(Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue()))
                            .update("status", "deleted");
                });
    }

    private void openPdf() {
        Intent intent = new Intent(this, AssignmentsPDFViewer.class);
        intent.putExtra("id", intentRecevied.getIntExtra("id", 0));
        startActivity(intent);

    }

    public void AssignmentDirect(View view) {
        Intent intent = new Intent(this, ListOfPeople.class);
        intent.putExtra("companyId", intentRecevied.getIntExtra("companyId", 0));
        intent.putExtra("createdByID", intentRecevied.getIntExtra("createdByID", 0));
        intent.putExtra("id", intentRecevied.getIntExtra("id", 0));
        intent.putExtra("inChargeID", intentRecevied.getIntExtra("inChargeID", 0));
        intent.putExtra("urgency", intentRecevied.getStringExtra("urgency"));
        intent.putExtra("urgencyNumber", intentRecevied.getIntExtra("urgencyNumber", 0));
        intent.putExtra("assignmentType", intentRecevied.getStringExtra("assignmentType"));
        intent.putExtra("company", intentRecevied.getStringExtra("company"));
        intent.putExtra("createdBy", intentRecevied.getStringExtra("createdBy"));
        intent.putExtra("date", intentRecevied.getStringExtra("date"));
        intent.putExtra("inCharge", intentRecevied.getStringExtra("inCharge"));
        intent.putExtra("note", intentRecevied.getStringExtra("note"));
        intent.putExtra("status", intentRecevied.getStringExtra("status"));
        intent.putExtra("time", intentRecevied.getStringExtra("time"));
        intent.putExtra("pdf", intentRecevied.getBooleanExtra("pdf", false));
        intent.putExtra("photo", intentRecevied.getBooleanExtra("photo", false));
        intent.putExtra("title", intentRecevied.getStringExtra("title"));
        intent.putExtra("due_date", intentRecevied.getStringExtra("due_date"));
        intent.putExtra("due_time", intentRecevied.getStringExtra("due_time"));

        startActivityForResult(intent, 2);
    }

    public void reloadWorkers(){
        workersList.clear();
        db.collection("Assignments").document(String.valueOf(id)).collection("Assigned").orderBy("id", Query.Direction.ASCENDING)
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
    }

    public void AssignmentFinish(View view) {
        Intent intent = new Intent(this, HomeMyAssignmentsFinish.class);
        intent.putExtra("id", intentRecevied.getIntExtra("id", 0));
        intent.putExtra("inChargeID", intentRecevied.getIntExtra("inChargeID", 0));
        intent.putExtra("createdByID", intentRecevied.getIntExtra("createdByID", 0));
        intent.putExtra("title", intentRecevied.getStringExtra("title"));
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentsDirectAdapter(this, workersList, this);
        recyclerView.setAdapter(adapter);
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
    public void onItemLongClick(int position, int workerID) {
        if (workersList.get(position).getId() != intentRecevied.getIntExtra("inChargeID", 0) && workersList.get(position).getId() != sharedPreferences.getInt("id", 0)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.choose_what_to_do)
                    .setItems(R.array.delete, (DialogInterface.OnClickListener) (dialog, which) -> {
                        if (!isConnected()){
                            Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
                        }
                        else if (which == 0){
                            delete(workerID, workersList.get(position).getId());
                        }
                        else {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
    }

    private void delete(int assignedID, int positionId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure_to_delete_worker).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(id)).collection("Assigned")
                    .document(String.valueOf(assignedID));

            documentReference.update("date_process", date);
            documentReference.update("time_process", time);
            documentReference.update("deleted", true).addOnSuccessListener(aVoid -> {
                DocumentReference docrefForDeletedUser = db.collection("Users").document(String.valueOf(positionId)).collection("Assignments").document(String.valueOf(id));
                docrefForDeletedUser.update("status", "removed");
            }).addOnCompleteListener(task -> {
                sendNotificationToWhomIsDeleted(positionId);
                Toast.makeText(this, "Seçilen çalışan silindi", Toast.LENGTH_SHORT).show();
                reloadWorkers();
            });
        }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
        builder.create().show();
    }

    private void sendNotificationToWhomIsDeleted(int assignedID) {
        Toast.makeText(this, String.valueOf(assignedID), Toast.LENGTH_SHORT).show();
        String titleString = intentRecevied.getStringExtra("title");
        String message = "Görevden silindiniz";
        Notifications notifications = new Notifications();
        notifications.sendNotification(String.valueOf(assignedID), this, titleString, message);
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void VoiceListener(View view) {
        player.start();
    }
}