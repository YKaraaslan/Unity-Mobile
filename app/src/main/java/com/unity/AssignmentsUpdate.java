package com.unity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.r0adkll.slidr.Slidr;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AssignmentsUpdate extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    Toolbar toolbar;
    int STORAGE_PERMISSION_CODE = 1, CAMERA_PERMISSION_CODE = 10, PICK_IMAGE = 2, CAMERA_PIC_REQUEST = 100, PICKFILE_REQUEST_CODE = 21, AUDIO_PERMISSION_CODE = 30;
    ;
    Spinner person, assignment_type, degree_of_urgency;
    ImageView imageView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    SharedPreferences sharedPreferences;
    TextInputEditText note;
    Button createAssignment;
    String selected = "none";
    Uri pdfUri;
    ProgressDialog progressDialog;
    ConstraintLayout pdf_layout, image_layout;
    TextView pdf_name, recorder_text;
    EditText title;
    Bitmap bitmap;
    ProgressBar progressBar;
    Uri photoUri;
    String inChargeOld;
    int assigned_max_id = 0;
    TextView date, time;
    boolean dateSet, timeSet, firstTimeEnteredForTitle = true, firstTimeEnteredToApp = true;
    boolean startedListening, recordedVoice, startedRecording, recordExists;

    AutoCompleteTextView company;
    FloatingActionButton fab_recorder, fab_listener;

    Map<String, Integer> workers = new HashMap<>();
    Map<String, Integer> workersMap = new HashMap<>();
    Map<String, Integer> companies = new HashMap<>();
    Map<String, Integer> urgencies = new HashMap<>();
    Map<String, Integer> urgenciesMap = new HashMap<>();
    Map<String, Integer> assignment_types = new HashMap<>();
    Map<String, Integer> assignment_typesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments_update);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        imageView = findViewById(R.id.image);
        person = findViewById(R.id.person);
        company = findViewById(R.id.company);
        assignment_type = findViewById(R.id.assignment_type);
        note = findViewById(R.id.note);
        createAssignment = findViewById(R.id.create_assignment);
        degree_of_urgency = findViewById(R.id.degree_of_urgency);
        pdf_layout = findViewById(R.id.pdf_layout);
        pdf_name = findViewById(R.id.pdf_name);
        title = findViewById(R.id.title);
        progressBar = findViewById(R.id.progressBar);
        image_layout = findViewById(R.id.image_layout);
        /*date = findViewById(R.id.date);
        time = findViewById(R.id.time);*/
        fab_recorder = findViewById(R.id.fab_recorder);
        fab_listener = findViewById(R.id.fab_listener);
        recorder_text = findViewById(R.id.recorder_text);

        createAssignment.setOnClickListener(view -> {
            if (Objects.requireNonNull(title.getText()).toString().equalsIgnoreCase("")) {
                Toast.makeText(this, "Başlık bos bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Objects.requireNonNull(note.getText()).toString().equalsIgnoreCase("") && !recordedVoice && !recordExists) {
                Toast.makeText(this, "Açıklama yazın ya da ses kaydı girin", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.are_you_sure_to_update_assignment).setPositiveButton(R.string.yes, (dialogInterface, i) -> Update()).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            });
            builder.create().show();
        });

        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0))).collection("Assigned")
                .orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    assigned_max_id = Objects.requireNonNull(task.getResult().getDocuments().get(0).getLong("id")).intValue() + 1;
                } catch (Exception ex) {
                    assigned_max_id = 1;
                }
            }
        });

        setImageOrDocument();
        init();
    }

    private void setImageOrDocument() {
        if (getIntent().getBooleanExtra("photo", false)) {
            image_layout.setVisibility(View.VISIBLE);
            pdf_layout.setVisibility(View.GONE);
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(String.valueOf(getIntent().getIntExtra("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> {
                photoUri = uri;
                Glide.with(this)
                        .load(uri.toString())
                        .centerCrop()
                        .into(imageView);
            }).addOnCompleteListener(task -> progressBar.setVisibility(View.GONE)).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
        } else if (getIntent().getBooleanExtra("pdf", false)) {
            image_layout.setVisibility(View.GONE);
            pdf_layout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            image_layout.setVisibility(View.GONE);
            pdf_layout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        pdf_layout.setOnClickListener(view -> openPdf());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        List<String> typeList = new ArrayList<>();

        db.collection("AssignmentsType").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int counter = 0;
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    typeList.add(document.getString("value"));
                    assignment_types.put(document.getString("value"), Objects.requireNonNull(document.getLong("id")).intValue());
                    assignment_typesMap.put(document.getString("value"), counter);
                    counter++;
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                assignment_type.setAdapter(dataAdapter);
                assignment_type.setSelection(Objects.requireNonNull(assignment_typesMap.get(getIntent().getStringExtra("assignmentType"))));
            }
        });

        /* ------------------------------------------------------------------- */

        List<String> workersList = new ArrayList<>();

        db.collection("Users").whereNotEqualTo("id", sharedPreferences.getInt("id", 0)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int counter = 0;
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if (Objects.requireNonNull(document.getBoolean("worker"))) {
                        workers.put(document.getString("name"), Objects.requireNonNull(document.getLong("id")).intValue());
                        workersList.add(document.getString("name"));
                        workersMap.put(document.getString("name"), counter);
                        counter++;
                    }
                }

                ArrayAdapter<String> personAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workersList);
                personAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                person.setAdapter(personAdapter);
                person.setSelection(Objects.requireNonNull(workersMap.get(getIntent().getStringExtra("inCharge"))));
                inChargeOld = getIntent().getStringExtra("inCharge");
            }
        });

        /* ----------------------------------------------------------------- */

        List<String> companiesList = new ArrayList<>();

        db.collection("Companies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    companies.put(document.getString("name"), Objects.requireNonNull(document.getLong("id")).intValue());
                    companiesList.add(document.getString("name"));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, companiesList);
                company.setThreshold(1);
                company.setAdapter(adapter);
            }
        });

        List<String> urgencyList = new ArrayList<>();

        db.collection("UrgencyTypes").orderBy("valueNumber", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int counter = 0;
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    urgencies.put(document.getString("value"), Objects.requireNonNull(document.getLong("valueNumber")).intValue());
                    urgencyList.add(document.getString("value"));
                    urgenciesMap.put(document.getString("value"), counter);
                    counter++;
                }

                ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urgencyList);
                urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                degree_of_urgency.setAdapter(urgencyAdapter);
                degree_of_urgency.setSelection(Objects.requireNonNull(urgenciesMap.get(getIntent().getStringExtra("urgency"))));
            }
        });

        try {
            setValues();
        } catch (Exception ignored) {
        }

        /* ---------------------------------------- */

        if (title.getText().toString().equals("")) {
            title.setText("Unity Görev");

            title.setOnTouchListener((v, event) -> {
                if (firstTimeEnteredForTitle) {
                    firstTimeEnteredForTitle = false;
                    title.setText("");
                }
                return false;
            });
        }

        StorageReference storageReference = storage.getReference();
        storageReference.child("AssignmentsAudio").child(String.valueOf(getIntent().getIntExtra("id", 0))).getDownloadUrl().addOnSuccessListener(uri -> {
            if (uri != null) {
                recordExists = true;
            }
        });

        fab_recorder.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
    }

    private void setValues() {
        title.setText(getIntent().getStringExtra("title"));
        note.setText(getIntent().getStringExtra("note"));
        company.setText(getIntent().getStringExtra("company"));

        if (getIntent().getStringExtra("due_date") != null || getIntent().getStringExtra("due_time") != null) {
            date.setText(getIntent().getStringExtra("due_date"));
            time.setText(getIntent().getStringExtra("due_time"));

            if (Objects.requireNonNull(getIntent().getStringExtra("due_date")).equalsIgnoreCase("")) {
                date.setText("__/__/____");
            }
            if (Objects.requireNonNull(getIntent().getStringExtra("due_time")).equalsIgnoreCase("")) {
                date.setText("__:__");
            }
        }
    }

    private void openPdf() {
        Intent intent = new Intent(this, AssignmentsPDFViewer.class);
        intent.putExtra("id", getIntent().getIntExtra("id", 0));
        startActivity(intent);
    }

    public void UploadPhoto(View view) {
        if (ContextCompat.checkSelfPermission(AssignmentsUpdate.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getGallery();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGallery();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCamera();
            }
        } else if (requestCode == PICKFILE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDocuments();
            }
        }
    }

    private void getDocuments() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        Intent i = Intent.createChooser(intent, "Pdf Seçiniz");
        startActivityForResult(i, PICKFILE_REQUEST_CODE);
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
        if (ContextCompat.checkSelfPermission(AssignmentsUpdate.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else if (ContextCompat.checkSelfPermission(AssignmentsUpdate.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
        } else {
            getCamera();
        }
    }

    private void getCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Görev Resmi");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Kameradan çekildi.");
        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CAMERA_PIC_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            photoUri = Objects.requireNonNull(data).getData();
            startCropping(photoUri);
            selected = "photo";
            pdf_layout.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
        if (resultCode == RESULT_OK && requestCode == CAMERA_PIC_REQUEST && data != null) {
            startCropping(photoUri);
            selected = "photo";
            pdf_layout.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }

        if (resultCode == RESULT_OK && requestCode == PICKFILE_REQUEST_CODE && data != null) {
            pdfUri = data.getData();
            pdf_layout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            selected = "document";
            bitmap = null;
            pdf_name.setText(setName(pdfUri));
        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            photoUri = UCrop.getOutput(Objects.requireNonNull(data));
            imageView.postInvalidate();
            Glide.with(this)
                    .load(photoUri.toString())
                    .centerCrop()
                    .into(imageView);
        }
    }

    private void startCropping(Uri photoUri) {
        String destinationFileName = "CropImage";
        destinationFileName += ".jpeg";

        UCrop uCrop = UCrop.of(photoUri, Uri.fromFile(new File(this.getCacheDir(), destinationFileName)));

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

    private String setName(Uri pdfUri) {
        String uriString = pdfUri.toString();
        File myFile = new File(uriString);
        String displayName = null;

        if (uriString.startsWith("content://")) {
            try (Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        return displayName;
    }

    public void Update() {
        if (!companies.containsKey(company.getText().toString())) {
            progressDialog.dismiss();
            Toast.makeText(this, "Girilen firma ismine ait kayit bulunamadi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isConnected()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Internet baglantisi bulunamadi", Toast.LENGTH_SHORT).show();
            return;
        }
        /*if (!dateSet || !timeSet){
            Toast.makeText(this, "Tarih ve saati kontrol ediniz", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            Date d1 = sdformat.parse(date1);
            Date d2 = sdformat.parse(date.getText().toString() + " " + time.getText().toString());

            if (d1 != null) {
                if (d1.compareTo(d2) > 0) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Görev geçmiş bir tarihe kurulamaz", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        progressDialog = new ProgressDialog(AssignmentsUpdate.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setTitle(getString(R.string.uploading));
        progressDialog.show();


        if (recordedVoice) {
            String filename = String.valueOf(getIntent().getIntExtra("id", 0));
            StorageReference storageReference = storage.getReference();
            Uri uri = Uri.fromFile(new File(AudiosName));
            storageReference.child("AssignmentsAudio").child(filename).putFile(uri).addOnProgressListener(snapshot -> {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            });
        }

        if (photoUri != null) {
            String filename = String.valueOf(getIntent().getIntExtra("id", 0));
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(filename).putFile(photoUri).addOnProgressListener(snapshot -> {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }).addOnSuccessListener(taskSnapshot -> FinishUpdating(true, false)).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Görev Güncellendi esnasinda bir problemle karsilasildi", Toast.LENGTH_SHORT).show();
            });
        } else if (pdfUri != null) {
            String filename = String.valueOf(getIntent().getIntExtra("id", 0));
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(filename).putFile(pdfUri).addOnProgressListener(snapshot -> {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }).addOnSuccessListener(taskSnapshot -> FinishUpdating(false, true)).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Görev Güncellendi esnasinda bir problemle karsilasildi", Toast.LENGTH_SHORT).show();
            });
        } else {
            FinishUpdating(false, false);
        }
    }

    private void FinishUpdating(boolean photoSelected, boolean pdfSelected) {
        String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        DocumentReference documentReference = db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));

        documentReference.update("title", title.getText().toString().trim());
        documentReference.update("note", Objects.requireNonNull(note.getText()).toString().trim());
        documentReference.update("photo", photoSelected);
        documentReference.update("pdf", pdfSelected);
        documentReference.update("inCharge", person.getSelectedItem());
        documentReference.update("inChargeID", workers.get(person.getSelectedItem().toString()));
        documentReference.update("company", company.getText().toString().trim());
        documentReference.update("companyID", companies.get(company.getText().toString()));
        documentReference.update("urgency", degree_of_urgency.getSelectedItem().toString().trim());
        documentReference.update("urgencyNumber", urgencies.get(degree_of_urgency.getSelectedItem().toString()));
        documentReference.update("assignmentType", assignment_type.getSelectedItem());
        /*documentReference.update("date_due", date.getText().toString());
        documentReference.update("time_due", time.getText().toString());*/

        documentReference.collection("UpdatedBy").add(new AssignmentsUpdateItems(sharedPreferences.getString("name", ""),
                dateString, sharedPreferences.getInt("id", 0), FieldValue.serverTimestamp()));

        if (!inChargeOld.equalsIgnoreCase(String.valueOf(person.getSelectedItem()))) {
            CollectionReference collectionReference = documentReference.collection("Assigned");

            int receivedID = Objects.requireNonNull(workers.get(person.getSelectedItem().toString()));

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                boolean exists = false;
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    if (Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue() == Objects.requireNonNull(workers.get(person.getSelectedItem().toString()))) {
                        exists = true;
                    }
                }
                if (!exists) {
                    DoesNotExist(workers.get(person.getSelectedItem().toString()), collectionReference);
                }
            }).addOnCompleteListener(task -> {
                DocumentReference ourDocumentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Assignments")
                        .document(String.valueOf(getIntent().getIntExtra("id", 0)));

                ourDocumentReference.update("inChargeID", receivedID);
                ourDocumentReference.update("title", title.getText().toString().trim());
                ourDocumentReference.update("note", Objects.requireNonNull(note.getText()).toString().trim());
            });

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    DocumentReference assignedUserDocumentReferences = db.collection("Users").document(String.valueOf(Objects.requireNonNull(document.getLong("userID")).intValue()))
                            .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));

                    assignedUserDocumentReferences.update("inChargeID", receivedID);
                    assignedUserDocumentReferences.update("title", title.getText().toString().trim());
                    assignedUserDocumentReferences.update("note", Objects.requireNonNull(note.getText()).toString().trim());
                }
            }).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                Toast.makeText(AssignmentsUpdate.this, "Görev Güncellendi", Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            int receivedID = Objects.requireNonNull(workers.get(person.getSelectedItem().toString()));
            CollectionReference collectionReference = documentReference.collection("Assigned");

            DocumentReference ourDocumentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Assignments")
                    .document(String.valueOf(getIntent().getIntExtra("id", 0)));

            ourDocumentReference.update("inChargeID", receivedID);
            ourDocumentReference.update("title", title.getText().toString().trim());
            ourDocumentReference.update("note", Objects.requireNonNull(note.getText()).toString().trim());

            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    DocumentReference assignedUserDocumentReferences = db.collection("Users").document(String.valueOf(Objects.requireNonNull(document.getLong("userID")).intValue()))
                            .collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)));
                    assignedUserDocumentReferences.update("inChargeID", receivedID);
                    assignedUserDocumentReferences.update("title", title.getText().toString().trim());
                    assignedUserDocumentReferences.update("note", Objects.requireNonNull(note.getText()).toString().trim());
                }
            }).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                Toast.makeText(AssignmentsUpdate.this, "Görev Güncellendi", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void DoesNotExist(Integer receivedID, CollectionReference collectionReference) {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        collectionReference.document(String.valueOf(assigned_max_id)).set(new ListOfPeopleAssigningItems(1, receivedID, person.getSelectedItem().toString(), date, time,
                sharedPreferences.getString("name", "------"), sharedPreferences.getInt("id", 0), false, "", "", "", "", false));

        db.collection("Users").document(String.valueOf(receivedID)).collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)))
                .set(new AssignmentsUserDBItems(title.getText().toString(), Objects.requireNonNull(note.getText()).toString(), "active", date, time, "", "", "", getIntent().getIntExtra("id", 0), Objects.requireNonNull(workers.get(person.getSelectedItem().toString())),
                        sharedPreferences.getInt("id", 0)));

        sendNotification();
    }

    private void sendNotification() {
        db.collection("Assignments").document(String.valueOf(getIntent().getIntExtra("id", 0)))
                .collection("Assigned")
                .whereEqualTo("deleted", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                String titleString = title.getText().toString().trim();
                String message = "Görev güncellendi";
                Notifications notifications = new Notifications();
                notifications.sendNotification(String.valueOf(Objects.requireNonNull(documentSnapshot.getLong("userID")).intValue()), this, titleString, message);
            }
        });
    }

    public void UploadDocument(View view) {
        if (ContextCompat.checkSelfPermission(AssignmentsUpdate.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getDocuments();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void AddCompany(View view) {
        startActivity(new Intent(this, CompanyAdd.class));
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
        /*date.setText(i2 + "/" + (i1+1) + "/" + i);
        dateSet = true;*/
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        /*time.setText(i + ":" + i1);
        timeSet = true;*/
    }

    MediaRecorder recorder;
    String AudiosName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRecording.m4a";

    public void VoiceRecorder(View view) {
        if (ContextCompat.checkSelfPermission(AssignmentsUpdate.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(AssignmentsUpdate.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, AUDIO_PERMISSION_CODE);
            return;
        }

        if (view.getId() == R.id.fab_recorder) {
            startRecordingAudio();
        } else {
            startListening();
        }
    }

    private void startRecordingAudio() {
        if (!startedRecording) {
            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setAudioEncodingBitRate(16 * 44100);
                recorder.setAudioSamplingRate(44100);

                recorder.setOutputFile(AudiosName);
                firstTimeEnteredToApp = false;
                recorder.prepare();
                recorder.start();
                startedRecording = true;
                fab_listener.setVisibility(View.GONE);

                fab_recorder.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                recorder_text.setText("Kayıt Ediliyor...");
                recorder_text.setTextColor(Color.GREEN);

            } catch (Exception ex) {
                Toast.makeText(this, "Ses kaydedici başlatılamadı", Toast.LENGTH_SHORT).show();
                Log.d("AssigmentCreate", "RecordVoice: " + ex);
                startedRecording = false;
                fab_recorder.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                recorder_text.setText("Kayıt Başarısız!");
                recorder_text.setTextColor(Color.RED);
                fab_listener.setVisibility(View.GONE);
                recordedVoice = false;
            }
        } else {
            try {
                recorder.stop();
                recorder.release();
                fab_listener.setVisibility(View.VISIBLE);
                startedRecording = false;
                fab_recorder.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                recorder_text.setText("Kayıt Edildi!");
                recorder_text.setTextColor(Color.BLUE);
                recordedVoice = true;
            } catch (Exception ex) {
                startedRecording = false;
                fab_recorder.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                recorder_text.setText("Kayıt Başarısız!");
                recorder_text.setTextColor(Color.RED);
                fab_listener.setVisibility(View.GONE);
                recordedVoice = false;
            }
        }
    }

    private void startListening() {
        MediaPlayer player = new MediaPlayer();
        if (!startedRecording) {
            try {
                player.setDataSource(AudiosName);
                player.prepare();
                player.start();
                startedListening = true;

                fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
                recorder_text.setText("Kayıt Dinleniyor...");
                recorder_text.setTextColor(Color.GREEN);
            } catch (Exception ex) {
                fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                recorder_text.setText("Kayıt Dinlemedi!");
                recorder_text.setTextColor(Color.RED);
                startedListening = false;
            }
        } else {
            Toast.makeText(this, "Kayıt esnasında dinleme yapılamaz", Toast.LENGTH_SHORT).show();
            startedListening = false;
        }
    }
}