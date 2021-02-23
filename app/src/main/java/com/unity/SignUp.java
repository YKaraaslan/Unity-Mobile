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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.r0adkll.slidr.Slidr;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SignUp extends AppCompatActivity {

    Toolbar toolbar;
    EditText nameSurname, company, username, password, password_again, mail, phone, admin_confirmation;
    String nameSurnameString, companyString, usernameString, passwordString, passwordAgainString, mailString, phoneString;
    Button signUp;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    int userOnHoldMaxID = 0, constantMaxID = 0;
    int STORAGE_PERMISSION_CODE = 1, CAMERA_PERMISSION_CODE = 10, PICK_IMAGE = 2, CAMERA_PIC_REQUEST = 100, PICKFILE_REQUEST_CODE = 21;
    Uri photoUri;
    CircularImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        nameSurname = findViewById(R.id.nameSurname);
        company = findViewById(R.id.company);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        password_again = findViewById(R.id.password_again);
        mail = findViewById(R.id.mail);
        phone = findViewById(R.id.phone);
        signUp = findViewById(R.id.sign_up);
        imageView = findViewById(R.id.image);

        signUp.setOnClickListener(view -> SignUpEvent());

        db.collection("UsersOnHold").orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                userOnHoldMaxID = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { userOnHoldMaxID = 1;}
        });
    }

    public void SignUpEvent() {
        nameSurnameString = nameSurname.getText().toString();
        companyString = company.getText().toString();
        usernameString = username.getText().toString();
        passwordString = password.getText().toString();
        passwordAgainString = password_again.getText().toString();
        mailString = mail.getText().toString();
        phoneString = phone.getText().toString();

        if (nameSurnameString.equals("") || companyString.equals("") || usernameString.equals("") || passwordString.equals("") || passwordAgainString.equals("") || mailString.equals("") ||
                phoneString.equals("")) {
            Toast.makeText(this, getString(R.string.blank), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!passwordString.equals(passwordAgainString)) {
            Toast.makeText(this, getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordString.length() < 5 || passwordString.length() > 16) {
            Toast.makeText(this, getString(R.string.password_length), Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoUri == null){
            Toast.makeText(this, getString(R.string.select_image), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.confirm_signup)).setMessage(getString(R.string.confirm_signup_text))
                .setPositiveButton(R.string.ok, (dialogInterface, i1) -> {
                    progressDialog = new ProgressDialog(SignUp.this);
                    progressDialog.setTitle(getString(R.string.signing_up));
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();
                    constantMaxID = userOnHoldMaxID;
                    signUpDatabase();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i12) -> {

        }).show();
    }

    private void signUpDatabase() {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        db.collection("UsersOnHold").document(String.valueOf(constantMaxID))
                .set(new SignUpItems(nameSurnameString, mailString, phoneString, usernameString, passwordString, "", date, time, "", "", 0,
                        constantMaxID, false, "", "", 0))
                .addOnCompleteListener(task -> {
                    sendNotification();
                    uploadPhoto();
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task1 -> {
                        if (task.isSuccessful()) {
                            DocumentReference document = db.collection("UsersOnHoldTokens").document(String.valueOf(constantMaxID));
                            document.set(new TokenItems(task1.getResult()));
                        }
                    });
                });
    }

    private void uploadPhoto() {
        String filename = String.valueOf(constantMaxID);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        storageReference.child("UsersOnHold").child(filename).putFile(photoUri).addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(SignUp.this, getString(R.string.user_signed), Toast.LENGTH_LONG).show();
            startActivity(new Intent(SignUp.this, MainActivity.class));
            finish();
        });
    }

    private void sendNotification() {
        db.collection("Users").whereEqualTo("authorization", 1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                String title = "Bir kullanıcı kayıt onayı beklemekte";
                String message = "Kayıt onayı bekleyen kullanıcı: " + nameSurnameString;
                Notifications notifications = new Notifications();
                notifications.sendNotification(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("id")).intValue()), SignUp.this, title, message);
            }
        });
    }

    public void UploadPhoto(View view) {
        if(ContextCompat.checkSelfPermission(SignUp.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
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
        if(ContextCompat.checkSelfPermission(SignUp.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else if(ContextCompat.checkSelfPermission(SignUp.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
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

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            try {
                final Uri imageUri = Objects.requireNonNull(data).getData();
                photoUri = imageUri;
                final InputStream imageStream = getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
                imageView.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK && requestCode == CAMERA_PIC_REQUEST && data != null) {
            Bitmap image = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            imageView.setImageBitmap(image);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Objects.requireNonNull(image).compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "Photo", null);
            photoUri = Uri.parse(path);
            imageView.setVisibility(View.VISIBLE);
        }
    }

}