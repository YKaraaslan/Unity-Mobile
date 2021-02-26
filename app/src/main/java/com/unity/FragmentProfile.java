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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.unity.HomePageClient.CurrentActivity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class FragmentProfile extends Fragment {
    Toolbar toolbar;
    CircularImageView workersImage;
    TextView workers_name, workers_description, rating_text, projects_done_text, projects_todo_text, projects_completed_text;
    TextView worker_phone, worker_mail, project_todo_linearLayout_text, project_done_linearLayout_text, project_completed_linearLayout_text;
    RatingBar rating;
    LinearLayout projects_done_layout, projects_todo_layout, project_completed_linearLayout, projects_completed_layout;
    LinearLayout complaint_linearLayout, project_done_linearLayout, project_todo_linearLayout;
    Button edit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    AtomicInteger activeCounter = new AtomicInteger();
    AtomicInteger passiveCounter = new AtomicInteger();
    AtomicInteger completedCounter = new AtomicInteger();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;
    private static final String KEY_UPDATE_URL = "updateVisitApk";
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        workersImage = view.findViewById(R.id.workersImage);
        workers_name = view.findViewById(R.id.workers_name);
        workers_description = view.findViewById(R.id.workers_description);
        rating_text = view.findViewById(R.id.rating_text);
        projects_done_text = view.findViewById(R.id.projects_done_text);
        projects_todo_text = view.findViewById(R.id.projects_todo_text);
        worker_phone = view.findViewById(R.id.worker_phone);
        worker_mail = view.findViewById(R.id.worker_mail);
        project_todo_linearLayout_text = view.findViewById(R.id.project_todo_linearLayout_text);
        project_done_linearLayout_text = view.findViewById(R.id.project_done_linearLayout_text);

        rating = view.findViewById(R.id.rating);
        projects_done_layout = view.findViewById(R.id.projects_done_layout);
        projects_todo_layout = view.findViewById(R.id.projects_todo_layout);
        complaint_linearLayout = view.findViewById(R.id.complaint_linearLayout);
        project_done_linearLayout = view.findViewById(R.id.project_done_linearLayout);
        project_todo_linearLayout = view.findViewById(R.id.project_todo_linearLayout);
        edit = view.findViewById(R.id.edit);
        project_completed_linearLayout_text =  view.findViewById(R.id.project_completed_linearLayout_text);
        project_completed_linearLayout = view.findViewById(R.id.project_completed_linearLayout);
        projects_completed_layout = view.findViewById(R.id.projects_completed_layout);
        projects_completed_text = view.findViewById(R.id.projects_completed_text);

        sharedPreferences = requireActivity().getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();

        setImage();

        projects_done_layout.setOnClickListener(view1 -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setMessage("Tamamlanan Görev Sayısı: " + passiveCounter);
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {

            }).show();
        });
        projects_todo_layout.setOnClickListener(view2 -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setMessage("Aktif Görev Sayısı: " + activeCounter);
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {

            }).show();
        });
        projects_completed_layout.setOnClickListener(view3 -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
            alertDialogBuilder.setMessage("Biten Görev Sayısı: " + completedCounter);
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {

            }).show();
        });
        complaint_linearLayout.setOnClickListener(view13 -> {

        });
        project_done_linearLayout.setOnClickListener(view14 -> {
            Intent intent = new Intent(requireContext(), WorkersInformationAssignments.class);
            intent.putExtra("id", sharedPreferences.getInt("id", 0));
            intent.putExtra("status", "passive");
            startActivity(intent);
        });
        project_todo_linearLayout.setOnClickListener(view15 -> {
            Intent intent = new Intent(requireContext(), WorkersInformationAssignments.class);
            intent.putExtra("id", sharedPreferences.getInt("id", 0));
            intent.putExtra("status", "active");
            startActivity(intent);
        });

        project_completed_linearLayout.setOnClickListener(view16 -> {
            Intent intent = new Intent(requireContext(), WorkersInformationAssignments.class);
            intent.putExtra("id", sharedPreferences.getInt("id", 0));
            intent.putExtra("status", "completed");
            startActivity(intent);
        });

        edit.setOnClickListener(view16 -> startActivityForResult(new Intent(requireContext(), ProfileEdit.class), 1));

        setTextViews();
        init();
        CurrentActivity.setCurrectActivity("FragmentProfile");
        return view;
    }

    private void setImage() {
        storageReference.child("Users").child(String.valueOf(sharedPreferences.getInt("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(requireContext())
                .load(uri.toString())
                .centerCrop()
                .into(workersImage));
    }


    @SuppressLint("SetTextI18n")
    private void init() {
        db.collection("Assignments").orderBy("id", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    db.collection("Assignments").document(documentSnapshot.getId()).collection("Assigned")
                            .whereEqualTo("userID", sharedPreferences.getInt("id", 0))
                            .whereEqualTo("deleted", false)
                            .get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            for (int i = 0; i < Objects.requireNonNull(task1.getResult()).size(); i++) {
                                if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("active")){
                                    activeCounter.getAndIncrement();
                                    projects_todo_text.setText(String.valueOf(activeCounter));
                                    project_todo_linearLayout_text.setText("Aktif Görevler (" + activeCounter + ")");
                                }
                                else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("passive")){
                                    passiveCounter.getAndIncrement();
                                    projects_done_text.setText(String.valueOf(passiveCounter));
                                    project_done_linearLayout_text.setText("Tamamlanan Görevler (" + passiveCounter + ")");
                                }
                                else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("completed")){
                                    completedCounter.getAndIncrement();
                                    projects_completed_text.setText(String.valueOf(completedCounter));
                                    project_completed_linearLayout_text.setText("Biten Görevler (" + completedCounter + ")");
                                }
                            }
                        }
                    });

                    if (Objects.requireNonNull(documentSnapshot.getLong("createdByID")).intValue() == sharedPreferences.getInt("id", 0)){
                        if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("active")){
                            activeCounter.getAndIncrement();
                            projects_todo_text.setText(String.valueOf(activeCounter));
                            project_todo_linearLayout_text.setText("Aktif Görevler (" + activeCounter + ")");
                        }
                        else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("passive")){
                            passiveCounter.getAndIncrement();
                            projects_done_text.setText(String.valueOf(passiveCounter));
                            project_done_linearLayout_text.setText("Tamamlanan Görevler (" + passiveCounter + ")");
                        }
                        else if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("completed")){
                            completedCounter.getAndIncrement();
                            projects_completed_text.setText(String.valueOf(completedCounter));
                            project_completed_linearLayout_text.setText("Biten Görevler (" + completedCounter + ")");
                        }
                    }
                }

            }
        });
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

    private void setTextViews() {
        workers_name.setText(sharedPreferences.getString("name", "------"));
        workers_description.setText(sharedPreferences.getString("position", "------"));
        worker_phone.setText(sharedPreferences.getString("phone", "phone"));
        worker_mail.setText(sharedPreferences.getString("mail", "phone"));
    }

    private void Unsubscribe() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("finance")
                .addOnCompleteListener(task -> Log.d("HOMEPAGE", "UNSUBSCRIBED"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            setImage();
        }
    }
}
