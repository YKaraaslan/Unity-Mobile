package com.unity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsersOnHold extends AppCompatActivity implements UsersOnHoldCallBack{
    Toolbar toolbar;
    RecyclerView recyclerView;
    UsersOnHoldAdapter adapter;
    List<UsersOnHoldItems> userList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_on_hold);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        recyclerView = findViewById(R.id.recycler_view);

        setupRecyclerView();


        db.collection("UsersOnHold").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                userList.add(new UsersOnHoldItems(documentSnapshot.getString("name"), documentSnapshot.getString("mail"), documentSnapshot.getString("phone"), documentSnapshot.getString("username"),
                        documentSnapshot.getString("password"), documentSnapshot.getString("position"), documentSnapshot.getString("date_applied"), documentSnapshot.getString("time_applied"),
                        documentSnapshot.getString("date_result"), documentSnapshot.getString("time_result"), Objects.requireNonNull(documentSnapshot.getLong("authorization")).intValue(), Objects.requireNonNull(documentSnapshot.getLong("id")).intValue(),
                        Objects.requireNonNull(documentSnapshot.getBoolean("worker")), documentSnapshot.getString("online"), documentSnapshot.getString("handledBy"),
                        Objects.requireNonNull(documentSnapshot.getLong("handledByID")).intValue()));
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersOnHoldAdapter(this, userList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, UsersOnHoldInformation.class);
        intent.putExtra("name", userList.get(position).getName());
        intent.putExtra("mail", userList.get(position).getMail());
        intent.putExtra("phone", userList.get(position).getPhone());
        intent.putExtra("username", userList.get(position).getUsername());
        intent.putExtra("password", userList.get(position).getPassword());
        intent.putExtra("position", userList.get(position).getPosition());
        intent.putExtra("date_applied", userList.get(position).getDate_applied());
        intent.putExtra("time_applied", userList.get(position).getTime_applied());
        intent.putExtra("authorization", userList.get(position).getAuthorization());
        intent.putExtra("id", userList.get(position).getId());
        intent.putExtra("online", userList.get(position).getOnline());
        startActivity(intent);
    }
}