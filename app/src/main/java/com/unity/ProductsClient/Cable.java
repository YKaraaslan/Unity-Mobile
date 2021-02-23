package com.unity.ProductsClient;

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
import com.unity.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cable extends AppCompatActivity implements WebGuideCallBack {
    Toolbar toolbar;
    RecyclerView recyclerView;
    WebGuideAdapter adapter;
    List<WebGuideItems> webGuideList = new ArrayList<>();
    boolean order = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cable);
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

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            order = extras.getBoolean("order");
        }
    }

    private void init() {
        for (int i = 0; i < 10; i++){
            webGuideList.add(new WebGuideItems(R.drawable.unity, "Kablo Ürünleri"));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WebGuideAdapter(this, webGuideList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, ImageView imageContainer, TextView name) {
        if (order) {
            // TODO: 27/08/2020 Tidy up this code
            int rnd = new Random().nextInt(4);
            Intent intent = new Intent(Cable.this, CableProductOrders.class);
            intent.putExtra("type", String.valueOf(rnd));
            startActivity(intent);
        } else {
            startActivity(new Intent(this, CableProductInformation.class));
        }
    }
}