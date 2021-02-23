package com.unity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;
import com.unity.HomePageClient.CurrentActivity;

import java.util.Objects;

public class MessageArchive extends AppCompatActivity {
    Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    SharedPreferences sharedPreferences;
    FirestoreRecyclerOptions<FragmentContactItem> options;
    MessageArchiveAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_archive);
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
        setupRecyclerView();

        CurrentActivity.setCurrectActivity("FragmentContact");
    }

    private void setupRecyclerView() {
        Query query = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .orderBy("time_server", Query.Direction.DESCENDING).whereEqualTo("situation", "archive");
        options = new FirestoreRecyclerOptions.Builder<FragmentContactItem>()
                .setQuery(query, FragmentContactItem.class)
                .build();
        adapter = new MessageArchiveAdapter(options,this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MessageArchiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot) {
                Intent intent = new Intent(MessageArchive.this, Message.class);
                intent.putExtra("userName", documentSnapshot.getString("user_name"));
                intent.putExtra("userID", Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageArchive.this);
                builder.setTitle(R.string.choose_what_to_do)
                        .setItems(R.array.delete_or_return_back_to_messages, (dialog, which) -> {
                            if (which == 0){
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(MessageArchive.this);
                                builder2.setMessage(R.string.are_you_sure_to_delete_message)
                                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> delete(documentSnapshot.getString("dbID"), documentSnapshot.getLong("userID").intValue()))
                                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

                                        });
                                builder2.create().show();
                            }
                            else if (which == 1){
                                returnBackToMessages(documentSnapshot.getString("dbID"));
                            }
                            else {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });
    }

    private void returnBackToMessages(String dbID) {
        DocumentReference documentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .document(dbID);

        documentReference.update("situation", "active");
    }

    private void delete(String dbID, int userID) {
        DocumentReference documentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .document(dbID);

        documentReference.update("situation", "passive");

        CollectionReference messagesToReadTheirs = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessagesToRead");
        messagesToReadTheirs.whereEqualTo("senderID", userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshots : queryDocumentSnapshots){
                messagesToReadTheirs.document(documentSnapshots.getId()).delete();
            }
        });

        documentReference.collection("Messages").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshots : queryDocumentSnapshots){
                documentReference.collection("Messages").document(documentSnapshots.getId()).delete();
            }
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
