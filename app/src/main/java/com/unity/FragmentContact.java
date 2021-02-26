package com.unity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.unity.HomePageClient.CurrentActivity;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FragmentContact extends Fragment{
    FloatingActionButton fab, archive;
    Toolbar toolbar;
    RecyclerView recyclerView;
    FragmentContactAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirestoreRecyclerOptions<FragmentContactItem> options;
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private static final String KEY_UPDATE_URL = "updateVisitApk";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        recyclerView = view.findViewById(R.id.recycler_view);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), MessageUsers.class)));

        archive = view.findViewById(R.id.archive);
        archive.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), MessageArchive.class)));

        sharedPreferences = requireActivity().getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();

        setupRecyclerView();

        CurrentActivity.setCurrectActivity("FragmentContact");
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_page, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/html");
                String title = getString(R.string.invitation_message) + "\n\n" + mFirebaseRemoteConfig.getString(KEY_UPDATE_URL);
                share.putExtra(Intent.EXTRA_TEXT, title);
                startActivity(share);
                break;
            case R.id.action_info:
                Toast.makeText(requireActivity(), "Yakında aktifleşecektir.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_reminder:
                startActivity(new Intent(getContext(), Reminder.class));
                break;
            case R.id.action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setMessage(getString(R.string.are_you_sure_to_exit));
                builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    sharedEditor.clear().apply();
                    Unsubscribe();
                    startActivity(new Intent(getContext(), MainActivity.class));
                    requireActivity().finish();
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Unsubscribe() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("finance")
                .addOnCompleteListener(task -> Log.d("HOMEPAGE", "UNSUBSCRIBED"));
    }

    private void setupRecyclerView() {
        Query query = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .orderBy("time_server", Query.Direction.DESCENDING).whereEqualTo("situation", "active");
        options = new FirestoreRecyclerOptions.Builder<FragmentContactItem>()
                .setQuery(query, FragmentContactItem.class)
                .build();
        adapter = new FragmentContactAdapter(options, requireActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new FragmentContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot) {
                Intent intent = new Intent(requireActivity(), Message.class);
                intent.putExtra("userName", documentSnapshot.getString("user_name"));
                intent.putExtra("userID", Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle(R.string.choose_what_to_do)
                        .setItems(R.array.delete_or_archive, (dialog, which) -> {
                            if (which == 0){
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(requireActivity());
                                builder2.setMessage(R.string.are_you_sure_to_delete_message)
                                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> delete(documentSnapshot.getString("dbID"), documentSnapshot.getLong("userID").intValue()))
                                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

                                        });
                                builder2.create().show();
                            }
                            else if (which == 1){
                                archive(documentSnapshot.getString("dbID"), documentSnapshot.getLong("userID").intValue());
                            }
                            else {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });
    }

    private void archive(String dbID, int userID) {
        DocumentReference documentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .document(dbID);

        documentReference.update("situation", "archive");

        CollectionReference messagesToReadTheirs = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessagesToRead");
        messagesToReadTheirs.whereEqualTo("senderID", userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshots : queryDocumentSnapshots){
                messagesToReadTheirs.document(documentSnapshots.getId()).delete();
            }
        });
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
