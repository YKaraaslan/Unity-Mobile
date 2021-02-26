package com.unity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ReminderUpdate extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    Toolbar toolbar;
    TextView date, time;
    TextInputEditText title, note;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    int max_id;
    ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_update);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        title = findViewById(R.id.title);
        note = findViewById(R.id.note);

        getMaxID();
        setValues();
    }

    private void setValues() {
        title.setText(getIntent().getStringExtra("title"));
        note.setText(getIntent().getStringExtra("note"));
        date.setText(getIntent().getStringExtra("date"));
        time.setText(getIntent().getStringExtra("time"));
    }

    private void getMaxID() {
        registration = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Notes")
                .orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
                    assert value != null;
                    try{
                        max_id = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
                    }
                    catch (Exception ex) { max_id = 1;}
                });
    }

    public void showDatePickerDialog(View view) {
        DialogFragment datePicker = new PickerDate();
        datePicker.show(getSupportFragmentManager(), "Tarih");
    }

    public void showTimePickerDialog(View view) {
        DialogFragment timePicker = new PickerTime();
        timePicker.show(getSupportFragmentManager(), "Saat");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        date.setText(i2 + "/" + (i1+1) + "/" + i);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        time.setText(i + ":" + i1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    public void UpdateReminder(View view) {
        if (Objects.requireNonNull(title.getText()).toString().trim().equals("") || Objects.requireNonNull(note.getText()).toString().trim().equals("") || date.getText().toString().isEmpty() ||
                time.getText().toString().isEmpty()){
            Toast.makeText(this, "Alanlar boş bırakılamaz", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            Date d1 = sdformat.parse(date1);
            Date d2 = sdformat.parse(date.getText().toString() + " " + time.getText().toString());

            if (d1 != null) {
                if (d1.compareTo(d2) > 0) {
                    Toast.makeText(this, "Hatırlatıcı geçmiş bir tarihe ayarlanamaz", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String timeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        ReminderItem item = new ReminderItem(max_id, Objects.requireNonNull(title.getText()).toString(), Objects.requireNonNull(note.getText()).toString(), dateString, timeString,
                Objects.requireNonNull(date.getText()).toString(), Objects.requireNonNull(time.getText()).toString(), "active");*/

        DocumentReference noteReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Notes")
                .document(String.valueOf(getIntent().getIntExtra("id", 0)));

        noteReference.update("title", title.getText().toString().trim());
        noteReference.update("note", note.getText().toString().trim());
        noteReference.update("date_end", date.getText().toString().trim());
        noteReference.update("time_end", time.getText().toString().trim()).addOnCompleteListener(task -> {
            Toast.makeText(ReminderUpdate.this, "Notunuz Güncellendi", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}