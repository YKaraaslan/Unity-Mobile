package com.unity.ProductsClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.r0adkll.slidr.Slidr;
import com.unity.OrdersWebGuideActuator;
import com.unity.OrdersWebGuideDevice;
import com.unity.OrdersWebGuideSensor;
import com.unity.OrdersWebGuideSystems;
import com.unity.R;

import java.util.Objects;

public class WebGuideProductOrders extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_guide_product_orders);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // TODO: 27/08/2020 DeleteThisLine
        getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersWebGuideDevice()).commit();

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            switch (Objects.requireNonNull(extras.getString("type"))){
                case "1":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersWebGuideDevice()).commit();
                    break;
                case "2":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersWebGuideSensor()).commit();
                    break;
                case "3":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersWebGuideActuator()).commit();
                    break;
                case "4":
                    getSupportFragmentManager().beginTransaction().replace(R.id.order_frame, new OrdersWebGuideSystems()).commit();
                    break;
            }
        }
    }
}