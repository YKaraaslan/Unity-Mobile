package com.unity.HomeAssignmentsAll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.unity.AssignmentsCreateItems;
import com.unity.AssignmentsDirect;
import com.unity.AssignmentsUserDBItems;
import com.unity.HomePageClient.HomeMyAssignmentsActiveAdapter;
import com.unity.HomePageClient.HomeMyAssignmentsCompletedAdapter;
import com.unity.HomePageClient.HomeMyAssignmentsCompletedCallBack;
import com.unity.HomePageClient.HomeMyAssignmentsCompletedInformation;
import com.unity.HomePageClient.HomeMyAssignmentsItems;
import com.unity.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class HomeAssignmentsCompleted extends Fragment {
    RecyclerView recyclerView;
    HomeAssignmentsAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    FirestoreRecyclerOptions<AssignmentsCreateItems> options;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_my_services_pager_active_services, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progressBar);
        sharedPreferences = requireActivity().getSharedPreferences("Log", MODE_PRIVATE);
        setupRecyclerView();
        progressBar.setVisibility(View.GONE);
        return view;
    }

    private void setupRecyclerView() {
        Query query = db.collection("Assignments").orderBy("id", Query.Direction.DESCENDING).whereEqualTo("status", "passive");
        options = new FirestoreRecyclerOptions.Builder<AssignmentsCreateItems>()
                .setQuery(query, AssignmentsCreateItems.class)
                .build();
        adapter = new HomeAssignmentsAdapter(options, requireActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new HomeAssignmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot) {
                db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue())).get()
                        .addOnSuccessListener(documentSnapshotSecond -> {
                            Intent intent = new Intent(requireContext(), HomeMyAssignmentsCompletedInformation.class);
                            intent.putExtra("companyId", Objects.requireNonNull(documentSnapshotSecond.getLong("companyID")).intValue());
                            intent.putExtra("createdByID", Objects.requireNonNull(documentSnapshotSecond.getLong("createdByID")).intValue());
                            intent.putExtra("id", Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue());
                            intent.putExtra("inChargeID", Objects.requireNonNull(documentSnapshotSecond.getLong("inChargeID")).intValue());
                            intent.putExtra("urgency", documentSnapshotSecond.getString("urgency"));
                            intent.putExtra("urgencyNumber", Objects.requireNonNull(documentSnapshotSecond.getLong("urgencyNumber")).intValue());
                            intent.putExtra("assignmentType", documentSnapshotSecond.getString("assignmentType"));
                            intent.putExtra("company", documentSnapshotSecond.getString("company"));
                            intent.putExtra("createdBy", documentSnapshotSecond.getString("createdBy"));
                            intent.putExtra("date", documentSnapshotSecond.getString("date"));
                            intent.putExtra("inCharge", documentSnapshotSecond.getString("inCharge"));
                            intent.putExtra("note", documentSnapshotSecond.getString("note"));
                            intent.putExtra("status", documentSnapshotSecond.getString("status"));
                            intent.putExtra("time", documentSnapshotSecond.getString("time"));
                            intent.putExtra("pdf", Objects.requireNonNull(documentSnapshotSecond.getBoolean("pdf")));
                            intent.putExtra("photo", Objects.requireNonNull(documentSnapshotSecond.getBoolean("photo")));
                            intent.putExtra("title", documentSnapshotSecond.getString("title"));
                            intent.putExtra("guarantee", Objects.requireNonNull(documentSnapshotSecond.getBoolean("guarantee")));
                            intent.putExtra("service_price", Objects.requireNonNull(documentSnapshotSecond.getBoolean("service_price")));
                            intent.putExtra("bill", Objects.requireNonNull(documentSnapshotSecond.getBoolean("bill")));
                            intent.putExtra("finishNote", documentSnapshotSecond.getString("finishNote"));
                            intent.putExtra("finishDate", documentSnapshotSecond.getString("finishDate"));
                            intent.putExtra("finishTime", documentSnapshotSecond.getString("finishTime"));
                            intent.putExtra("due_date", documentSnapshotSecond.getString("due_date"));
                            intent.putExtra("due_time",documentSnapshotSecond.getString("due_time"));
                            intent.putExtra("finance", false);

                            startActivity(intent);
                        });
            }

            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot) {

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