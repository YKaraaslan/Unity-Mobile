package com.unity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AssignmentsImageViewer extends AppCompatActivity {
    ImageView image;
    ProgressBar progressBar;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments_image_viewer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        image = findViewById(R.id.image);
        progressBar = findViewById(R.id.progressBar);

        StorageReference storageReference = storage.getReference().child("Assignments").child(String.valueOf(getIntent().getIntExtra("id", 0)));
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(image)).addOnCompleteListener(task -> progressBar.setVisibility(View.GONE)).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }
}