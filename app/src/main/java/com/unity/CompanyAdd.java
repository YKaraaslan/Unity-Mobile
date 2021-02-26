package com.unity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;

import java.util.Objects;

public class CompanyAdd extends AppCompatActivity {
    Toolbar toolbar;
    EditText name, city, district, in_charge_name, position, mail, phone;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    int assignedMaxID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_add);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        in_charge_name = findViewById(R.id.in_charge_name);
        position = findViewById(R.id.position);
        mail = findViewById(R.id.mail);
        phone = findViewById(R.id.phone);
        district = findViewById(R.id.district);
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        db.collection("Companies").orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                assignedMaxID = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { assignedMaxID = 1;}
        });
    }

    public void AddCompany(View view) {
        if (name.getText().toString().equals("") || phone.getText().toString().equals("")){
            Toast.makeText(this, "İsim veya telefon zorunlu alanlardır.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.company_add_text))
                .setPositiveButton(R.string.ok, (dialogInterface, i1) -> Add()).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();
    }

    private void Add() {
        CompanyAddItems item = new CompanyAddItems(name.getText().toString().trim(), city.getText().toString().trim(), district.getText().toString().trim(),
                in_charge_name.getText().toString().trim(), mail.getText().toString().trim(), phone.getText().toString().trim(), position.getText().toString().trim(), assignedMaxID);

        db.collection("Companies").document(String.valueOf(assignedMaxID))
                .set(item).addOnCompleteListener(task -> {
                    Toast.makeText(CompanyAdd.this, "Şirket Başarıyla Eklendi", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(CompanyAdd.this, "Ekleme işlemi başarısız. Lütfen internet bağlantınızı kontrol ediniz.", Toast.LENGTH_LONG).show());
    }
}