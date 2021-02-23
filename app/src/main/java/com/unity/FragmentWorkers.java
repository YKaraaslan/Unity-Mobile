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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.util.Pair;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.unity.HomePageClient.CurrentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FragmentWorkers extends Fragment {
    Toolbar toolbar;
    RecyclerView recyclerView;
    FragmentWorkersAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressBar progressBar;
    CollectionReference userRef;
    FirestoreRecyclerOptions<FragmentWorkersItem> options;
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private static final String KEY_UPDATE_URL = "updateVisitApk";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_workers, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progressBar);

        userRef = db.collection("Users");
        setupRecyclerView();

        sharedPreferences = requireActivity().getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();


        CurrentActivity.setCurrectActivity("FragmentWorkers");
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

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
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
        Query query = userRef.orderBy("online", Query.Direction.DESCENDING);
        options = new FirestoreRecyclerOptions.Builder<FragmentWorkersItem>()
                .setQuery(query, FragmentWorkersItem.class)
                .build();
        adapter = new FragmentWorkersAdapter(options, requireActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(documentSnapshot -> {
            if (documentSnapshot.getLong("id").intValue() != sharedPreferences.getInt("id", 0)){
                Intent intent = new Intent(getContext(), WorkersInformation.class);
                intent.putExtra("id", Objects.requireNonNull(documentSnapshot.getLong("id")).intValue());
                intent.putExtra("name", documentSnapshot.getString("name"));
                intent.putExtra("position", documentSnapshot.getString("position"));
                intent.putExtra("online", documentSnapshot.getString("online"));
                startActivity(intent);
            }
        });

        progressBar.setVisibility(View.GONE);
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
