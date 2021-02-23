package com.unity.HomePageClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.r0adkll.slidr.Slidr;
import com.unity.ProductsClient.AutomationControlProductOrders;
import com.unity.ProductsClient.Cable;
import com.unity.ProductsClient.CameraProductOrders;
import com.unity.R;
import com.unity.ProductsClient.TensionControl;
import com.unity.ProductsClient.WebGuide;

public class HomeOrders extends AppCompatActivity {
    Toolbar toolbar;
    CardView webGuide, tensionControl, camera, cable, automationControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_orders);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        webGuide = findViewById(R.id.webGuide);
        tensionControl = findViewById(R.id.tensionControl);
        camera = findViewById(R.id.camera);
        cable = findViewById(R.id.cable);
        automationControl = findViewById(R.id.automationControl);
        init();
    }

    private void init() {
        webGuide.setOnClickListener(view -> {
            Intent intent = new Intent(HomeOrders.this, WebGuide.class);
            intent.putExtra("order", true);
            startActivity(intent);
        });

        tensionControl.setOnClickListener(view -> {
            Intent intent = new Intent(HomeOrders.this, TensionControl.class);
            intent.putExtra("order", true);
            startActivity(intent);
        });

        camera.setOnClickListener(view -> {

            startActivity(new Intent(HomeOrders.this, CameraProductOrders.class));
        });

        cable.setOnClickListener(view -> {
            Intent intent = new Intent(HomeOrders.this, Cable.class);
            intent.putExtra("order", true);
            startActivity(intent);
        });

        automationControl.setOnClickListener(view -> {
            startActivity(new Intent(this, AutomationControlProductOrders.class));
        });
    }
}