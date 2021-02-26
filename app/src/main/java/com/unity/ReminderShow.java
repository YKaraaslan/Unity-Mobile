package com.unity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;

public class ReminderShow extends AppCompatActivity {
    Toolbar toolbar;
    TextInputEditText title, note;
    TextView date_created, date_end;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_show);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        title = findViewById(R.id.title);
        note = findViewById(R.id.note);
        date_created = findViewById(R.id.date_created);
        date_end = findViewById(R.id.date_end);

        title.setText(getIntent().getStringExtra("title"));
        note.setText(getIntent().getStringExtra("note"));

        String dateCreated = getIntent().getStringExtra("date") + " " + getIntent().getStringExtra("time");
        String dateEnd = getIntent().getStringExtra("date_end") + " " + getIntent().getStringExtra("time_end");

        date_created.setText(dateCreated);
        date_end.setText(dateEnd);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assignment_active_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit:
                Intent intent = new Intent(this, ReminderUpdate.class);
                intent.putExtra("id", getIntent().getIntExtra("id", 0));
                intent.putExtra("title", getIntent().getStringExtra("title"));
                intent.putExtra("note", getIntent().getStringExtra("note"));
                intent.putExtra("date", getIntent().getStringExtra("date"));
                intent.putExtra("time", getIntent().getStringExtra("time"));
                startActivity(intent);
                finish();
                break;

            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(ReminderShow.this);
                builder.setMessage(R.string.are_you_sure_to_delete_note).setPositiveButton(R.string.yes,
                        (dialogInterface, i) -> deleteReminder(getIntent().getIntExtra("id", 0))).setNegativeButton(R.string.cancel, (dialogInterface, i) -> { });
                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteReminder(int id) {
        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Notes").document(String.valueOf(id)).update("situation", "deleted")
                .addOnCompleteListener(task -> {
                    Toast.makeText(ReminderShow.this, "Notunuz Silindi", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}