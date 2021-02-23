package com.unity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.r0adkll.slidr.Slidr;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class ProfileEdit extends AppCompatActivity {

    Toolbar toolbar;
    EditText nameSurname, username, password, mail, phone;
    String nameSurnameString, usernameString, passwordString, mailString, phoneString;
    Button update;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    CircularImageView image;
    int STORAGE_PERMISSION_CODE = 1, PICK_IMAGE = 2;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    Uri uri;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        nameSurname = findViewById(R.id.nameSurname);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        mail = findViewById(R.id.mail);
        phone = findViewById(R.id.phone);
        update = findViewById(R.id.update);
        image = findViewById(R.id.image);

        update.setOnClickListener(view -> UpdateProfile());
        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();
        setTexts();

        image.setOnClickListener((View.OnClickListener) view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEdit.this);
            builder.setTitle(R.string.choose_what_to_do)
                    .setItems(R.array.image, (DialogInterface.OnClickListener) (dialog, which) -> {
                        if (which == 0){
                            if(ContextCompat.checkSelfPermission(ProfileEdit.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                                getGallery();
                            }
                            else {
                                ActivityCompat.requestPermissions(ProfileEdit.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            }
                        }
                    });
            builder.create().show();
        });

        getImage();
    }

    private void getImage() {
        storageReference.child("Users").child(String.valueOf(sharedPreferences.getInt("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri.toString())
                    .centerCrop()
                    .into(image);
        });
    }

    private void setTexts() {
        nameSurname.setText(sharedPreferences.getString("name", ""));
        username.setText(sharedPreferences.getString("username", ""));
        password.setText(sharedPreferences.getString("password", ""));
        mail.setText(sharedPreferences.getString("mail", ""));
        phone.setText(sharedPreferences.getString("phone", ""));
    }

    private void UpdateProfile() {
        nameSurnameString = nameSurname.getText().toString();
        usernameString = username.getText().toString();
        passwordString = password.getText().toString();
        mailString = mail.getText().toString();
        phoneString = phone.getText().toString();

        if (nameSurnameString.equals("") || usernameString.equals("") || passwordString.equals("") || mailString.equals("") ||
                phoneString.equals("")) {
            Toast.makeText(this, getString(R.string.blank), Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordString.length() < 5 || passwordString.length() > 16) {
            Toast.makeText(this, getString(R.string.password_length), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.confirm_update))
                .setPositiveButton(R.string.yes, (dialogInterface, i1) -> {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(getString(R.string.updating));
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();
                    signUpDatabase();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();
    }

    private void signUpDatabase() {
        if (!isConnected()){
            progressDialog.dismiss();
            Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference documentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0)));

        documentReference.update("name", nameSurnameString);
        documentReference.update("username", usernameString);
        documentReference.update("password", passwordString);
        documentReference.update("mail", mailString);
        documentReference.update("phone", phoneString).addOnCompleteListener(task -> {

            sharedEditor.putString("name", nameSurnameString);
            sharedEditor.putString("mail", mailString);
            sharedEditor.putString("password", passwordString);
            sharedEditor.putString("phone", phoneString);
            sharedEditor.putString("username", usernameString);
            sharedEditor.apply();

            setResult(RESULT_OK);

            Toast.makeText(ProfileEdit.this, "Profiliniz güncellendi!", Toast.LENGTH_SHORT).show();
            finish();
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getGallery();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            final Uri imageUri = Objects.requireNonNull(data).getData();
            if (imageUri != null) {
                startCropping(imageUri);
            }
        }
        else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.confirm_update_photo))
                    .setPositiveButton(R.string.yes, (dialogInterface, i1) -> {
                        uri = UCrop.getOutput(data);
                        uploadPhoto(uri);
                    }).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

            }).show();
        }
    }

    private void startCropping(Uri imageUri) {
        String destinationFileName = "CropImage";
        destinationFileName += ".jpeg";

        UCrop uCrop = UCrop.of(imageUri, Uri.fromFile(new File(this.getCacheDir(), destinationFileName)));

        uCrop.withMaxResultSize(700, 700);
        uCrop.withOptions(getCropOptions());

        uCrop.start(this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);
        options.setStatusBarColor(Color.BLACK);
        options.setToolbarColor(Color.WHITE);
        options.setToolbarTitle(getString(R.string.edit_image));
        return options;
    }

    private void uploadPhoto(Uri resultUri) {
        ProgressDialog progressDialogProgress = new ProgressDialog(this);
        progressDialogProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialogProgress.setProgress(0);
        progressDialogProgress.setTitle(getString(R.string.uploading));
        progressDialogProgress.setCancelable(false);
        progressDialogProgress.show();

        String filename = String.valueOf(sharedPreferences.getInt("id", 0));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        storageReference.child("Users").child(filename).putFile(resultUri).addOnProgressListener(snapshot -> {
            int currentProgress = (int) (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
            progressDialogProgress.setProgress(currentProgress);
        }).addOnSuccessListener(taskSnapshot -> {
            storageReference.child("Users").child(String.valueOf(sharedPreferences.getInt("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri.toString())
                        .centerCrop()
                        .into(image);
                Toast.makeText(ProfileEdit.this, "Profil Resminiz güncellendi!", Toast.LENGTH_SHORT).show();
                progressDialogProgress.dismiss();
            });
        }).addOnFailureListener(e -> {
            progressDialogProgress.dismiss();
            Toast.makeText(this, "Profil Resminiz güncellenemedi", Toast.LENGTH_SHORT).show();
        });
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}