package com.unity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UsersImageViewer extends AppCompatActivity {
    ImageView image;
    Intent intent;
    ProgressBar progressBar;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_image_viewer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        image = findViewById(R.id.image);
        progressBar = findViewById(R.id.progressBar);
        intent = getIntent();

        image.setImageBitmap(intent.getParcelableExtra("image"));

        if(intent.getParcelableExtra("image") == null){
            setImage();
        }
    }

    private void setImage() {
        StorageReference storageReference = storage.getReference();
        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.child(intent.getStringExtra("document")).child(String.valueOf(intent.getIntExtra("id", 0))).getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            image.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(Throwable::printStackTrace);

        progressBar.setVisibility(View.GONE);
    }
}