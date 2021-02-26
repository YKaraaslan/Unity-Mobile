package com.unity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.r0adkll.slidr.Slidr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AssignmentNotes extends AppCompatActivity implements AssignmentNotesCallBack {
    Toolbar toolbar;
    RecyclerView recyclerView;
    AssignmentNotesAdapter adapter;
    List<AssignmentNotesItems> notesList = new ArrayList<>();
    FloatingActionButton fab;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigment_notes);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        progressBar = findViewById(R.id.progressBar);

        fab.setOnClickListener(view1 -> {
            Intent intent = new Intent(this, AssignmentNotesAdd.class);
            intent.putExtra("id", getIntent().getIntExtra("id", 0));
            intent.putExtra("title", getIntent().getStringExtra("title"));
            intent.putExtra("createdByID", getIntent().getIntExtra("createdByID", 0));
            startActivityForResult(intent, 1);
        });

        try{
            setSeen();
        }
        catch (Exception ignored) { }
        setupRecyclerView();
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentNotesAdapter(this, notesList, this);
        recyclerView.setAdapter(adapter);
    }

    private void init() {
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes")
                .orderBy("id", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (Objects.requireNonNull(task.getResult()).size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    handleThis(documentSnapshot);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void handleThis(QueryDocumentSnapshot documentSnapshot) {
        if (!Objects.requireNonNull(documentSnapshot.getBoolean("deleted"))){
            notesList.add(new AssignmentNotesItems(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue(), Objects.requireNonNull(documentSnapshot.getLong("created_by_id")).intValue(),
                    documentSnapshot.getString("created_by"), documentSnapshot.getString("position"), documentSnapshot.getString("note"),
                    documentSnapshot.getString("date"), documentSnapshot.getString("time"), Objects.requireNonNull(documentSnapshot.getBoolean("deleted")),
                    Objects.requireNonNull(documentSnapshot.getBoolean("updated")), documentSnapshot.getString("dateProcess"), documentSnapshot.getString("timeProcess")));
        }
    }

    @Override
    public void onItemClick(int position) {
        if (notesList.get(position).getCreated_by_id() == sharedPreferences.getInt("id", 0)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.choose_what_to_do)
                    .setItems(R.array.what_to_do, (dialog, which) -> {
                        if (which == 0){
                            delete(notesList.get(position).getId());
                        }
                        else if (which == 1){
                            update(notesList.get(position).getId(), notesList.get(position).getNote());
                        }
                        else if (which == 2){
                            openSeenBy(notesList.get(position).getId());
                        }
                        else {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.choose_what_to_do)
                    .setItems(R.array.information, (dialog, which) -> {
                        if (which == 0){
                            openSeenBy(notesList.get(position).getId());
                        }
                        else {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
    }

    private void delete(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure_to_delete).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
            if (!isConnected()){
                Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
                return;
            }
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes")
                    .document(String.valueOf(id));

            documentReference.update("dateProcess", date);
            documentReference.update("timeProcess", time);
            documentReference.update("deleted", true).addOnCompleteListener(task -> {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        Toast.makeText(this, "Notunuz silindi.", Toast.LENGTH_SHORT).show();
                    });
        }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> { });
        builder.create().show();
    }

    private void update(int id, String note) {
        Intent intent = new Intent(this, AssignmentNotesAdd.class);
        intent.putExtra("update_id", id);
        intent.putExtra("update_document_id", getIntent().getIntExtra("id", 0));
        intent.putExtra("update_note", note);
        intent.putExtra("update_situation", true);
        intent.putExtra("title", getIntent().getStringExtra("title"));
        startActivityForResult(intent, 5);
    }

    private void setSeen() {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        CollectionReference collectionReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Notes");

        Log.d("DURUM", String.valueOf(getIntent().getIntExtra("id", 0)));
        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                Log.d("DURUM", documentSnapshot.getId());

                DocumentReference documentReference = collectionReference.document(documentSnapshot.getId()).collection("SeenBy").document(String.valueOf(sharedPreferences.getInt("id", 0)));
                documentReference.get().addOnSuccessListener(documentSnapshotSecond -> {
                    Log.d("DURUM", documentReference.getPath());

                    if (documentSnapshotSecond.getString("seen_date") == null){
                        documentReference.set(new AssignmentNotesSeenByItems(sharedPreferences.getInt("id", 0), sharedPreferences.getString("name", ""), sharedPreferences.getString("position", ""),
                                date, time));
                    }
                });
            }
        });
    }

    private void openSeenBy(int id) {
        Intent intentForActivity = new Intent(getApplicationContext(), AssignmentNotesSeenBy.class);
        intentForActivity.putExtra("id", id);
        intentForActivity.putExtra("assignment_id", getIntent().getIntExtra("id", 0));
        startActivity(intentForActivity);
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}