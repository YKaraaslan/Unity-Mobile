package com.unity.HomePageClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;
import com.unity.Message;
import com.unity.R;

import java.util.ArrayList;
import java.util.List;

public class HomeMyOrders extends AppCompatActivity implements HomeMyOrdersCallBack {
    Toolbar toolbar;
    RecyclerView recyclerView;
    HomeMyOrdersAdapter adapter;
    List<HomeMyOrdersItems> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_my_orders);
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
        init();
    }

    private void init() {
        for (int i = 0; i < 5; i++) {
            contactList.add(new HomeMyOrdersItems(i, "Siparislerim", "Aciklama", "13:00", R.drawable.unity));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HomeMyOrdersAdapter(this, contactList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, ImageView imageContainer, TextView name, TextView description, TextView time) {
        startActivity(new Intent(HomeMyOrders.this, Message.class));
    }
}