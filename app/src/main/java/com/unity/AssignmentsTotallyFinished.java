package com.unity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.r0adkll.slidr.Slidr;
import com.unity.HomePageClient.HomeMyAssignmentsItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignmentsTotallyFinished extends AppCompatActivity implements AssignmentsTotallyFinishedCallBack {
    Toolbar toolbar;
    RecyclerView recyclerView;
    AssignmentsTotallyFinishedAdapter adapter;
    List<HomeMyAssignmentsItems> list = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments_totally_finished);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        recyclerView = findViewById(R.id.recycler_view);
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        setupRecyclerView();
        init();
    }

    private void init() {
        db.collection("Assignments").orderBy("id", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    if (Objects.requireNonNull(documentSnapshot.getString("status")).equalsIgnoreCase("completed")){
                        handleThis(documentSnapshot);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void handleThis(QueryDocumentSnapshot document) {
        list.add(new HomeMyAssignmentsItems(document.getString("assignmentType"), document.getString("inCharge"), document.getString("createdBy"), document.getString("company"),
                document.getString("note"), document.getString("status"),
                Objects.requireNonNull(document.getLong("id")).intValue(), Objects.requireNonNull(document.getLong("inChargeID")).intValue(), Objects.requireNonNull(document.getLong("companyID")).intValue(),
                Objects.requireNonNull(document.getLong("createdByID")).intValue(), Objects.requireNonNull(document.getBoolean("photo")), Objects.requireNonNull(document.getBoolean("pdf")),
                document.getString("date"), document.getString("time"), Objects.requireNonNull(document.getLong("urgencyNumber")).intValue(), document.getString("urgency"),
                Objects.requireNonNull(document.getBoolean("guarantee")), Objects.requireNonNull(document.getBoolean("service_price")), Objects.requireNonNull(document.getBoolean("bill")),
                document.getString("finishNote"), document.getString("finishDate"), document.getString("finishTime"), document.getString("title"), document.getString("due_date"),
                document.getString("due_time")));
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentsTotallyFinishedAdapter(this, list, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, AssignmentsTotallyFinishedInformation.class);
        intent.putExtra("assignmentType", list.get(position).assignmentType);
        intent.putExtra("inCharge", list.get(position).inCharge);
        intent.putExtra("createdBy", list.get(position).createdBy);
        intent.putExtra("company", list.get(position).company);
        intent.putExtra("note", list.get(position).note);
        intent.putExtra("status", list.get(position).status);
        intent.putExtra("id", list.get(position).id);
        intent.putExtra("inChargeID", list.get(position).inChargeID);
        intent.putExtra("companyId", list.get(position).companyID);
        intent.putExtra("createdByID", list.get(position).createdByID);
        intent.putExtra("photo", list.get(position).photo);
        intent.putExtra("pdf", list.get(position).pdf);
        intent.putExtra("date", list.get(position).date);
        intent.putExtra("time", list.get(position).time);
        intent.putExtra("urgencyNumber", list.get(position).urgencyNumber);
        intent.putExtra("urgency", list.get(position).urgency);
        intent.putExtra("guarantee", list.get(position).guarantee);
        intent.putExtra("service_price", list.get(position).service_price);
        intent.putExtra("bill", list.get(position).bill);
        intent.putExtra("finishNote", list.get(position).finishNote);
        intent.putExtra("finishDate", list.get(position).finishDate);
        intent.putExtra("finishTime", list.get(position).finishTime);
        intent.putExtra("title", list.get(position).title);
        intent.putExtra("due_date", list.get(position).due_date);
        intent.putExtra("due_time", list.get(position).due_time);
        intent.putExtra("finance", false);

        startActivity(intent);
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
        ArrayList<HomeMyAssignmentsItems> filteredList = new ArrayList<>();
        for (HomeMyAssignmentsItems item : list) {
            if (item.getInCharge().toLowerCase().contains(text.toLowerCase().trim())) {
                filteredList.add(item);
            }
            else if (item.getCompany().toLowerCase().contains(text.toLowerCase().trim())) {
                filteredList.add(item);
            }
            else if (item.getTitle().toLowerCase().contains(text.toLowerCase().trim())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }
}