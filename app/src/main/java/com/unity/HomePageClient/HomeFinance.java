package com.unity.HomePageClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.r0adkll.slidr.Slidr;
import com.unity.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFinance extends AppCompatActivity implements HomeFinanceCallBack {
    Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    HomeFinanceAdapter adapter;
    List<HomeMyAssignmentsItems> peopleList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_finance);
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

        db.collection("Assignments").whereEqualTo("financial_situation", "active").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                handleThis(documentSnapshot);
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void handleThis(QueryDocumentSnapshot document) {
        peopleList.add(new HomeMyAssignmentsItems(document.getString("assignmentType"), document.getString("inCharge"), document.getString("createdBy"), document.getString("company"),
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
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HomeFinanceAdapter(this, peopleList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, HomeMyAssignmentsCompletedInformation.class);
        intent.putExtra("assignmentType", peopleList.get(position).assignmentType);
        intent.putExtra("inCharge", peopleList.get(position).inCharge);
        intent.putExtra("createdBy", peopleList.get(position).createdBy);
        intent.putExtra("company", peopleList.get(position).company);
        intent.putExtra("note", peopleList.get(position).note);
        intent.putExtra("status", peopleList.get(position).status);
        intent.putExtra("id", peopleList.get(position).id);
        intent.putExtra("inChargeID", peopleList.get(position).inChargeID);
        intent.putExtra("companyId", peopleList.get(position).companyID);
        intent.putExtra("createdByID", peopleList.get(position).createdByID);
        intent.putExtra("photo", peopleList.get(position).photo);
        intent.putExtra("pdf", peopleList.get(position).pdf);
        intent.putExtra("date", peopleList.get(position).date);
        intent.putExtra("time", peopleList.get(position).time);
        intent.putExtra("urgencyNumber", peopleList.get(position).urgencyNumber);
        intent.putExtra("urgency", peopleList.get(position).urgency);
        intent.putExtra("guarantee", peopleList.get(position).guarantee);
        intent.putExtra("service_price", peopleList.get(position).service_price);
        intent.putExtra("bill", peopleList.get(position).bill);
        intent.putExtra("finishNote", peopleList.get(position).finishNote);
        intent.putExtra("finishDate", peopleList.get(position).finishDate);
        intent.putExtra("finishTime", peopleList.get(position).finishTime);
        intent.putExtra("title", peopleList.get(position).title);
        intent.putExtra("due_date", peopleList.get(position).due_date);
        intent.putExtra("due_time", peopleList.get(position).due_time);
        intent.putExtra("finance", true);

        startActivity(intent);
    }
}