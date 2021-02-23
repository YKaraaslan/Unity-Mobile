package com.unity.HomePageClient;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unity.AssignmentsDirect;
import com.unity.AssignmentsUpdate;
import com.unity.AssignmentsDeleteItems;
import com.unity.AssignmentsUserDBItems;
import com.unity.FragmentWorkersAdapter;
import com.unity.FragmentWorkersItem;
import com.unity.Notifications;
import com.unity.R;
import com.unity.WorkersInformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class HomeMyAssignmentsActive extends Fragment {
    RecyclerView recyclerView;
    HomeMyAssignmentsActiveAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    FirestoreRecyclerOptions<AssignmentsUserDBItems> options;

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
        Query query = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Assignments")
                .orderBy("assignmentID", Query.Direction.DESCENDING).whereEqualTo("status", "active");
        options = new FirestoreRecyclerOptions.Builder<AssignmentsUserDBItems>()
                .setQuery(query, AssignmentsUserDBItems.class)
                .build();
        adapter = new HomeMyAssignmentsActiveAdapter(options, requireActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new HomeMyAssignmentsActiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot) {
                db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("assignmentID")).intValue())).get()
                        .addOnSuccessListener(documentSnapshotSecond -> {
                            Intent intent = new Intent(requireContext(), AssignmentsDirect.class);
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
                            intent.putExtra("due_date", documentSnapshotSecond.getString("due_date"));
                            intent.putExtra("due_time", documentSnapshotSecond.getString("due_time"));

                            startActivity(intent);
                        });
            }

            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot) {
                if (sharedPreferences.getInt("id", 0) == Objects.requireNonNull(documentSnapshot.getLong("createdByID")).intValue()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setTitle(R.string.choose_what_to_do)
                            .setItems(R.array.delete_or_change, (dialog, which) -> {
                                if (which == 0){
                                    delete(documentSnapshot);
                                }
                                else {
                                    edit(documentSnapshot);
                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    private void edit(DocumentSnapshot documentSnapshot) {
        db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("assignmentID")).intValue())).get()
                .addOnSuccessListener(documentSnapshotSecond -> {
                    Intent intent = new Intent(requireContext(), AssignmentsUpdate.class);
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
                    intent.putExtra("due_date", documentSnapshotSecond.getString("due_date"));
                    intent.putExtra("due_time", documentSnapshotSecond.getString("due_time"));

                    startActivity(intent);
                });
    }

    private void delete(DocumentSnapshot documentSnapshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.are_you_sure_to_delete_assignment).setMessage(R.string.deleted_assignments_cannot_return).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("assignmentID")).intValue())).collection("Assigned").whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots){
                        String titleString = sharedPreferences.getString("name", "") + " dahil olduğunuz görevi sildi.";
                        String message = "Silinen Görev: " + documentSnapshot.getString("title");
                        Notifications notifications = new Notifications();
                        notifications.sendNotification(String.valueOf(Objects.requireNonNull(doc.getLong("userID")).intValue()), requireContext(), titleString, message);
                    }
                });

                startDeleting(documentSnapshot);
            }
        }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> { });
        builder.create().show();
    }

    private void startDeleting(DocumentSnapshot documentSnapshot) {
        db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("assignmentID")).intValue())).get()
                .addOnSuccessListener(documentSnapshotSecond -> {
                    String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                    DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue()));
                    documentReference.update("status", "deleted");

                    documentReference.collection("DeletedBy").add(new AssignmentsDeleteItems(sharedPreferences.getString("name", ""), date, sharedPreferences.getInt("id", 0)))
                            .addOnSuccessListener(documentReference1 -> Toast.makeText(requireContext(), "Görev silindi.", Toast.LENGTH_SHORT).show());

                    documentReference.collection("Assigned").whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot assignedUserDocs : queryDocumentSnapshots){
                            DocumentReference documentReferenceForUser = db.collection("Users").document(String.valueOf(Objects.requireNonNull(assignedUserDocs.getLong("userID")).intValue()))
                                    .collection("Assignments").document(String.valueOf(Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue()));

                            documentReferenceForUser.update("status", "deleted");
                        }
                    });

                    db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Assignments")
                            .document(String.valueOf(Objects.requireNonNull(documentSnapshotSecond.getLong("id")).intValue()))
                            .update("status", "deleted");
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