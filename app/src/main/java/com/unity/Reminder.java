package com.unity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;

import java.util.Objects;

public class Reminder extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ReminderAdapter adapter;
    FirestoreRecyclerOptions<ReminderItem> options;
    FloatingActionButton fab;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
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
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(view1 -> startActivity(new Intent(this, ReminderAdd.class)));

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        Query query = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Notes")
                .orderBy("id", Query.Direction.DESCENDING).whereEqualTo("situation", "active");
        options = new FirestoreRecyclerOptions.Builder<ReminderItem>()
                .setQuery(query, ReminderItem.class)
                .build();
        adapter = new ReminderAdapter(options, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ReminderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int id, String title, String note, String date_created, String time_created, String date_end, String time_end) {
                Intent intent = new Intent(Reminder.this, ReminderShow.class);
                intent.putExtra("id", id);
                intent.putExtra("title", title);
                intent.putExtra("note", note);
                intent.putExtra("date", date_created);
                intent.putExtra("time", time_created);
                intent.putExtra("date_end", date_end);
                intent.putExtra("time_end", time_end);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(int id, String title, String note, String date_created, String time_created, String date_end, String time_end) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Reminder.this);
                builder.setTitle(R.string.choose_what_to_do)
                        .setItems(R.array.delete_or_change, (dialog, which) -> {
                            if (which == 0){
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(Reminder.this);
                                builder2.setMessage(R.string.are_you_sure_to_delete_note).setPositiveButton(R.string.yes,
                                        (dialogInterface, i) -> deleteReminder(id)).setNegativeButton(R.string.cancel, (dialogInterface, i) -> { });
                                builder2.create().show();

                            }
                            else {
                                editReminder(id, title, note, date_end, time_end);
                            }
                        });
                builder.create().show();
            }
        });
    }

    private void editReminder(int id, String title, String note, String date_end, String time_end) {
        Intent intent = new Intent(this, ReminderUpdate.class);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("date", date_end);
        intent.putExtra("time", time_end);
        startActivity(intent);
    }

    private void deleteReminder(int id) {
        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Notes").document(String.valueOf(id)).update("situation", "deleted")
                .addOnCompleteListener(task -> {
                    Toast.makeText(Reminder.this, "Notunuz Silindi", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}