package com.unity;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;


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

public class ListOfPeople extends AppCompatActivity implements ListOfPeopleCallBack{
    Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    ListOfPeopleAdapter adapter;
    List<ListOfPeopleItems> peopleList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    int assignment_max_id = 0, assigned_max_id = 0;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_people);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        intent = getIntent();

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recycler_view);
        setupRecyclerView();

        progressBar = findViewById(R.id.progressBar);

        db.collection("Users").whereEqualTo("worker", true)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if(Objects.requireNonNull(document.getLong("id")).intValue() != sharedPreferences.getInt("id", 0)
                    && Objects.requireNonNull(document.getLong("id")).intValue() != intent.getIntExtra("inChargeID", 0)
                    && Objects.requireNonNull(document.getLong("id")).intValue() != intent.getIntExtra("createdByID", 0)){
                        peopleList.add(new ListOfPeopleItems(Objects.requireNonNull(document.getLong("id")).intValue(),
                                document.getString("name")));
                    }
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });

        db.collection("Assignments").orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                assignment_max_id = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { assignment_max_id = 1;}
    });


        db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0))).collection("Assigned").
                orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                assigned_max_id = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { assigned_max_id = 1;}
        });
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListOfPeopleAdapter(this, peopleList, this);
        recyclerView.setAdapter(adapter);
    }

    private void Assign(int receivedID, String name) {
        if (!isConnected()){
            Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        db.collection("Users").document(String.valueOf(receivedID)).collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)))
                .set(new AssignmentsUserDBItems(getIntent().getStringExtra("title"), getIntent().getStringExtra("note"), "active", date, time, "", "", "",
                        intent.getIntExtra("id", 0), getIntent().getIntExtra("inChargeID", 0), getIntent().getIntExtra("createdByID", 0)))
                .addOnCompleteListener(task -> {
                    db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0))).collection("Assigned").
                            document(String.valueOf(assigned_max_id)).set(new ListOfPeopleAssigningItems(assigned_max_id, receivedID, name, date, time,
                            sharedPreferences.getString("name", "------"), sharedPreferences.getInt("id", 0), false, "" , "", "", "",false))
                            .addOnSuccessListener(aVoid -> {
                                sendNotificationToWhomIsChosen(String.valueOf(receivedID));
                            }).addOnCompleteListener(task1 -> {
                        sendNotificationToTheRest(name, receivedID);
                    });
                });
    }

    private void sendNotificationToWhomIsChosen(String receivedID) {
        String title = "Adınıza bir görev iletildi";
        String message = "İLETEN KİŞİ': " + sharedPreferences.getString("name", "-") + "\n\nİLETİLEN GÖREV: " + getIntent().getStringExtra("title");
        Notifications notifications = new Notifications();
        notifications.sendNotification(receivedID, this,
                title, message);
    }

    private void sendNotificationToTheRest(String name, int receivedID) {
        db.collection("Assignments").document(String.valueOf(intent.getIntExtra("id", 0)))
                .collection("Assigned")
                .whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                        if(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue() != receivedID && !Objects.requireNonNull(documentSnapshot.getBoolean("deleted")))
                        notificationSender(name, String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue()));
                    }
            notificationSender(name, String.valueOf(intent.getIntExtra("createdByID", 0)));
                }).addOnCompleteListener(task -> {
            Toast.makeText(ListOfPeople.this, "Gorev basariyla iletildi", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void notificationSender(String name, String receivedID) {
        String title = "Dahil olduğunuz görev yönlendirildi";
        String message = "GÖREV: " + getIntent().getStringExtra("title") + "\nYÖNLENDİRİLEN KİŞİ: " + name;
        Notifications notifications = new Notifications();
        notifications.sendNotification(receivedID, this,
                title, message);
    }

    @Override
    public void onItemClick(int position, int id, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.list_of_people_text) + " " + name)
                .setTitle(getString(R.string.list_of_people_title))
                .setPositiveButton(R.string.ok, (dialogInterface, i1) -> {
                    Assign(id, name);
                }).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        return true;
    }

    private void filter(String text) {
        ArrayList<ListOfPeopleItems> filteredList = new ArrayList<>();
        for (ListOfPeopleItems item : peopleList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase().trim())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }
}