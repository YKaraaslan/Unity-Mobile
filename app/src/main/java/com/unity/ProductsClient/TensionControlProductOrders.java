package com.unity.ProductsClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.r0adkll.slidr.Slidr;
import com.unity.OrdersTensionControlBrake;
import com.unity.OrdersTensionControlDevice;
import com.unity.OrdersTensionControlLoadcell;
import com.unity.R;

import java.util.Objects;

public class TensionControlProductOrders extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tension_control_product_orders);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // TODO: 27/08/2020 Delete This Line
        getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersTensionControlBrake()).commit();

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            switch (Objects.requireNonNull(extras.getString("type"))){
                case "1":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersTensionControlBrake()).commit();
                    break;
                case "2":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersTensionControlLoadcell()).commit();
                    break;
                case "3":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersTensionControlDevice()).commit();
                    break;
            }
            Toast.makeText(this, extras.getString("type"), Toast.LENGTH_SHORT).show();
        }
    }
}