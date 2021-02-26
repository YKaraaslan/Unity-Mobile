package com.unity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignmentNotesSeenBy extends AppCompatActivity implements AssignmentNotesSeenByCallBack{
    Toolbar toolbar;
    RecyclerView recyclerView;
    AssignmentNotesSeenByAdapter adapter;
    List<AssignmentNotesSeenByItems> seenByList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_notes_seen_by);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);

        setupRecyclerView();
        init();
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentNotesSeenByAdapter(this, seenByList, this);
        recyclerView.setAdapter(adapter);
    }


    private void init() {
        String notes = "Notes";
        if (getIntent().getBooleanExtra("finance", false)){
            notes = "NotesFinance";
        }
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("assignment_id", 0))).collection(notes)
        .document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("SeenBy").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                seenByList.add(new AssignmentNotesSeenByItems(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue(), documentSnapshot.getString("name"),
                        documentSnapshot.getString("position"), documentSnapshot.getString("seen_date"), documentSnapshot.getString("seen_time")));
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }).addOnSuccessListener(queryDocumentSnapshots -> progressBar.setVisibility(View.GONE));
    }


    @Override
    public void onItemClick(int position) {

    }
}