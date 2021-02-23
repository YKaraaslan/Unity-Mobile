package com.unity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.r0adkll.slidr.Slidr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ReminderAdd extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    Toolbar toolbar;
    TextView date, time;
    TextInputEditText title, note;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    int max_id;
    boolean dateSet, timeSet;
    ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);
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

    @SuppressLint("SimpleDateFormat")
    public void CreateReminder(View view) {
        if (Objects.requireNonNull(title.getText()).toString().trim().equals("") || Objects.requireNonNull(note.getText()).toString().trim().equals("") || !dateSet || !timeSet){
            Toast.makeText(this, "Alanlar boş bırakılamaz", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            Date d1 = sdformat.parse(date1);
            Date d2 = sdformat.parse(date.getText().toString() + " " + time.getText().toString());

            if (d1 != null) {
                if (d1.compareTo(d2) > 0) {
                    Toast.makeText(this, "Hatırlatıcı geçmiş bir tarihe kurulamaz", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*int hourParsed = Integer.parseInt(time.getText().toString().split(":")[0]);
        int minuteParsed = Integer.parseInt(time.getText().toString().split(":")[1]);

        int dayParsed = Integer.parseInt(date.getText().toString().split("/")[0]);
        int monthParsed = Integer.parseInt(date.getText().toString().split("/")[1]) - 1;
        int yearParsed = Integer.parseInt(date.getText().toString().split("/")[2]);

        //Log.d("DURUM", "Date :" + dayParsed + "/" + monthParsed + "/" + yearParsed + " " + hourParsed + ":" + minuteParsed);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourParsed);
        calendar.set(Calendar.MINUTE, minuteParsed);
        calendar.set(Calendar.DAY_OF_MONTH, dayParsed);
        calendar.set(Calendar.MONTH, monthParsed);
        calendar.set(Calendar.YEAR, yearParsed);

        Log.d("DURUM", "HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
        Log.d("DURUM", "MINUTE: " + calendar.get(Calendar.MINUTE));
        Log.d("DURUM", "DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
        Log.d("DURUM", "MONTH: " + calendar.get(Calendar.MONTH));
        Log.d("DURUM", "YEAR: " + calendar.get(Calendar.YEAR));
        Log.d("DURUM", "Calendar2: " + calendar.getTimeInMillis());

        createNotificationChannel();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(ReminderAdd.this, ReminderBroadcast.class);
        startService(intent);
        intent.putExtra("title", title.getText()).toString().trim();
        intent.putExtra("text", note.getText()).toString().trim();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ReminderAdd.this, 0, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);*/

        String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String timeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        ReminderItem item = new ReminderItem(max_id, Objects.requireNonNull(title.getText()).toString(), Objects.requireNonNull(note.getText()).toString(), dateString, timeString,
                Objects.requireNonNull(date.getText()).toString(), Objects.requireNonNull(time.getText()).toString(), "active");

        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Notes").document(String.valueOf(max_id))
                .set(item).addOnCompleteListener(task -> {
            Toast.makeText(this, "Notunuz Eklendi", Toast.LENGTH_SHORT).show();
            //setAlarm();
            finish();
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

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        date.setText(i2 + "/" + (i1+1) + "/" + i);
        dateSet = true;
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        time.setText(i + ":" + i1);
        timeSet = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
    }
}