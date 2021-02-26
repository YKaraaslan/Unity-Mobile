package com.unity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.r0adkll.slidr.Slidr;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class Services extends AppCompatActivity {
    Toolbar toolbar;
    int STORAGE_PERMISSION_CODE = 1, CAMERA_PERMISSION_CODE = 10, PICK_IMAGE = 2, CAMERA_PIC_REQUEST = 100;
    Spinner type, model, service_type;
    Chip chip_malfunction, chip_assembly;
    ImageView imageView;
    TextInputEditText explanation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        imageView = findViewById(R.id.image_services);
        type = findViewById(R.id.type);
        model = findViewById(R.id.model);
        service_type = findViewById(R.id.service_type);
        chip_malfunction = findViewById(R.id.chip_malfunction);
        chip_assembly = findViewById(R.id.chip_assembly);
        explanation = findViewById(R.id.explanation);

        init();
    }

    private void init() {

    }

    public void UploadPhoto(View view) {
        if(ContextCompat.checkSelfPermission(Services.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getGallery();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getGallery();
            }
        }
        else if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCamera();
            }
        }
    }

    @SuppressLint("IntentReset")
    private void getGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void UploadPhotoFromCamera(View view) {
        if(ContextCompat.checkSelfPermission(Services.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else if(ContextCompat.checkSelfPermission(Services.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
        }
        else {
            getCamera();
        }
    }

    private void getCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_PIC_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            try {
                final Uri imageUri = Objects.requireNonNull(data).getData();
                final InputStream imageStream = getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(Services.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK && requestCode == CAMERA_PIC_REQUEST) {
            Bitmap image = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            imageView.setImageBitmap(image);
        }
    }

    public void CreateService(View view) {
    }
}