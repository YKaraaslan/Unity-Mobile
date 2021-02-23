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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageUsers extends AppCompatActivity implements MessageUsersCallBack {
    Toolbar toolbar;
    RecyclerView recyclerView;
    MessageUsersAdapter adapter;
    List<MessageUsersItem> contactList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_users);
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
        init();
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(100);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageUsersAdapter(this, contactList, this);
        recyclerView.setAdapter(adapter);
    }

    private void init() {
        db.collection("Users").orderBy("id", Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                if (Objects.requireNonNull(documentSnapshot.getLong("id")).intValue() != sharedPreferences.getInt("id", 0)){
                    contactList.add(new MessageUsersItem(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue(), documentSnapshot.getString("name"),
                            documentSnapshot.getString("position"), documentSnapshot.getString("online"), R.drawable.unity));
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onItemClick(int position, int id, String name, String positionString) {
        Intent intent = new Intent(this, Message.class);
        intent.putExtra("userName", name);
        intent.putExtra("userID", id);
        intent.putExtra("position", positionString);
        startActivity(intent);
        finish();
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
        ArrayList<MessageUsersItem> filteredList = new ArrayList<>();
        for (MessageUsersItem item : contactList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase().trim())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }
}