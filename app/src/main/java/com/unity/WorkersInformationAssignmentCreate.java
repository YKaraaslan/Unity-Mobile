package com.unity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.r0adkll.slidr.Slidr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class WorkersInformationAssignmentCreate extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    Toolbar toolbar;
    int STORAGE_PERMISSION_CODE = 1, CAMERA_PERMISSION_CODE = 10, PICK_IMAGE = 2, CAMERA_PIC_REQUEST = 100, PICKFILE_REQUEST_CODE = 21, AUDIO_PERMISSION_CODE = 30;
    Spinner assignment_type, degree_of_urgency;
    ImageView imageView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    SharedPreferences sharedPreferences;
    TextInputEditText note;
    Button createAssignment;
    String selected = "none";
    Uri pdfUri, photoUri;
    ProgressDialog progressDialog;
    ConstraintLayout pdf_layout;
    TextView pdf_name, recorder_text;;
    EditText title;
    TextView date, time;
    boolean dateSet, timeSet, firstTimeEnteredForTitle = true, firstTimeEnteredToApp = true;
    boolean startedListening, recordedVoice, startedRecording;
    FloatingActionButton fab_recorder, fab_listener;

    AutoCompleteTextView company;

    Map<String, Integer> workers = new HashMap<>();
    Map<String, Integer> companies = new HashMap<>();
    Map<String, Integer> urgencies = new HashMap<>();

    int assignment_max_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workers_information_assignment_create);
        Slidr.attach(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        imageView = findViewById(R.id.image_services);
        company = findViewById(R.id.company);
        assignment_type = findViewById(R.id.assignment_type);
        note = findViewById(R.id.note);
        createAssignment = findViewById(R.id.create_assignment);
        degree_of_urgency = findViewById(R.id.degree_of_urgency);
        pdf_layout = findViewById(R.id.pdf_layout);
        pdf_name = findViewById(R.id.pdf_name);
        title = findViewById(R.id.title);
        fab_recorder = findViewById(R.id.fab_recorder);
        fab_listener = findViewById(R.id.fab_listener);
        recorder_text = findViewById(R.id.recorder_text);
        /*date = findViewById(R.id.date);
        time = findViewById(R.id.time);*/

        fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        createAssignment.setOnClickListener(view -> {
            if (Objects.requireNonNull(title.getText()).toString().equalsIgnoreCase("")) {
                Toast.makeText(this, "Başlık bos bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Objects.requireNonNull(note.getText()).toString().equalsIgnoreCase("") && !recordedVoice){
                Toast.makeText(this, "Açıklama yazın ya da ses kaydı girin", Toast.LENGTH_SHORT).show();
                return;
            }
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
            /*if (!dateSet || !timeSet) {
                Toast.makeText(this, "Tarih ve saati kontrol ediniz", Toast.LENGTH_SHORT).show();
                return;
            }*/
            progressDialog = new ProgressDialog(WorkersInformationAssignmentCreate.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setTitle(getString(R.string.uploading));
            progressDialog.show();
            CreateAssignment();
        });

        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        db.collection("Assignments").orderBy("id", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
            assert value != null;
            try{
                assignment_max_id = Objects.requireNonNull(value.getDocuments().get(0).getLong("id")).intValue() + 1;
            }
            catch (Exception ex) { assignment_max_id = 1;}
        });

        List<String> typeList = new ArrayList<String>();

        db.collection("AssignmentsType").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    typeList.add(document.getString("value"));
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                assignment_type.setAdapter(dataAdapter);
                try {
                    assignment_type.setSelection(3);
                } catch (Exception ignored) {
                }
            }
        });

        /* ------------------------------------------------------------------- */

        List<String> companiesList = new ArrayList<String>();

        AtomicReference<String> unity = new AtomicReference<>("");
        db.collection("Companies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    companies.put(document.getString("name"), Objects.requireNonNull(document.getLong("id")).intValue());
                    companiesList.add(document.getString("name"));
                    try {
                        if (Objects.requireNonNull(document.getLong("id")).intValue() == 574) {
                            unity.set(document.getString("name"));
                        }
                    } catch (Exception ignored) {
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, companiesList);
                company.setThreshold(1);
                company.setAdapter(adapter);
                try {
                    company.setText(unity.get());
                } catch (Exception ignored) {
                }
            }
        });

        List<String> urgencyList = new ArrayList<String>();

        db.collection("UrgencyTypes").orderBy("valueNumber", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    urgencies.put(document.getString("value"), Objects.requireNonNull(document.getLong("valueNumber")).intValue());
                    urgencyList.add(document.getString("value"));
                }

                ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, urgencyList);
                urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                degree_of_urgency.setAdapter(urgencyAdapter);
            }
        });

        /* ---------------------------------------- */

        title.setText("Unity Görev");

        title.setOnTouchListener((v, event) -> {
            if (firstTimeEnteredForTitle) {
                firstTimeEnteredForTitle = false;
                title.setText("");
            }
            return false;
        });
        fab_recorder.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
    }

    public void UploadPhoto(View view) {
        if(ContextCompat.checkSelfPermission(WorkersInformationAssignmentCreate.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
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
        else if (requestCode == PICKFILE_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
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
        if(ContextCompat.checkSelfPermission(WorkersInformationAssignmentCreate.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else if(ContextCompat.checkSelfPermission(WorkersInformationAssignmentCreate.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
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
                selected = "photo";
                pdf_layout.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                Toast.makeText(WorkersInformationAssignmentCreate.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK && requestCode == CAMERA_PIC_REQUEST && data != null) {
            Bitmap image = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            imageView.setImageBitmap(image);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Objects.requireNonNull(image).compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "Photo", null);
            photoUri = Uri.parse(path);
            pdf_layout.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            selected = "photo";
        }

        if (resultCode == RESULT_OK && requestCode == PICKFILE_REQUEST_CODE && data != null) {
            pdfUri = data.getData();
            pdf_layout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            selected = "document";

            pdf_name.setText(setName(pdfUri));
        }
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
        }
        else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        return displayName;
    }

    public void CreateAssignment() {
        /*try {
            SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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

        if (recordedVoice){
            String filename = String.valueOf(assignment_max_id);
            StorageReference storageReference = storage.getReference();
            Uri uri = Uri.fromFile(new File(AudiosName));
            storageReference.child("AssignmentsAudio").child(filename).putFile(uri).addOnProgressListener(snapshot -> {
                int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            });
        }

        if(selected.equalsIgnoreCase("photo")){
            String filename = String.valueOf(assignment_max_id);
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(filename).putFile(photoUri).addOnProgressListener(snapshot -> {
                int currentProgress = (int) (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }).addOnSuccessListener(taskSnapshot -> {
                saveAssignment(true, false);
                progressDialog.dismiss();
                Toast.makeText(this, "Gorev Olusturuldu", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Pdf yukleme esnasinda bir problemle karsilasildi", Toast.LENGTH_SHORT).show();
            });
        }
        else if (selected.equalsIgnoreCase("document")){
            String filename = String.valueOf(assignment_max_id);
            StorageReference storageReference = storage.getReference();
            storageReference.child("Assignments").child(filename).putFile(pdfUri).addOnProgressListener(snapshot -> {
                int currentProgress = (int) (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }).addOnSuccessListener(taskSnapshot -> {
                saveAssignment(false, true);
                progressDialog.dismiss();
                Toast.makeText(this, "Gorev Olusturuldu", Toast.LENGTH_SHORT).show();
                finish();

            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Pdf yukleme esnasinda bir problemle karsilasildi", Toast.LENGTH_SHORT).show();
            });
        }
        else{
            saveAssignment(false, false);
            Toast.makeText(this, "Gorev Olusturuldu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveAssignment(boolean photoSelected, boolean pdfSelected) {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        AssignmentsCreateItems item = new AssignmentsCreateItems(assignment_type.getSelectedItem().toString(), getIntent().getStringExtra("userName"),
                sharedPreferences.getString("name", ""), company.getText().toString(), Objects.requireNonNull(note.getText()).toString(), "active",
                assignment_max_id,  getIntent().getIntExtra("userID", 0),
                Objects.requireNonNull(companies.get(company.getText().toString())), sharedPreferences.getInt("id", 0), photoSelected, pdfSelected, date, time,
                Objects.requireNonNull(urgencies.get(degree_of_urgency.getSelectedItem().toString())),
                degree_of_urgency.getSelectedItem().toString(), false, false, false, "", "", "", "", title.getText().toString(), 0, "", "", "");

        db.collection("Assignments").document(String.valueOf(assignment_max_id))
                .set(item);

        db.collection("Assignments").document(String.valueOf(assignment_max_id)).collection("Assigned").document("1")
                .set(new ListOfPeopleAssigningItems(1, getIntent().getIntExtra("userID", 0), getIntent().getStringExtra("userName"), date, time,
                        sharedPreferences.getString("name", "------"), sharedPreferences.getInt("id", 0), false, "", "", "","", false));

        db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("Assignments").document(String.valueOf(assignment_max_id))
                .set(new AssignmentsUserDBItems(title.getText().toString(), note.getText().toString(), "active", date, time, "", "", "", assignment_max_id, getIntent().getIntExtra("userID", 0),
                        sharedPreferences.getInt("id", 0)));

        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("Assignments").document(String.valueOf(assignment_max_id))
                .set(new AssignmentsUserDBItems(title.getText().toString(), note.getText().toString(), "active", date, time, "", "", "", assignment_max_id, getIntent().getIntExtra("userID", 0),
                        sharedPreferences.getInt("id", 0)));

        String titleString = "Adınıza yeni bir görev oluşturuldu";
        String message = "GÖREV: " + title.getText().toString() + "\nAÇIKLAMA: " + note.getText().toString();
        Notifications notifications = new Notifications();
        notifications.sendNotification(String.valueOf(getIntent().getIntExtra("userID", 0)), this,
                titleString, message);
    }

    public void UploadDocument(View view) {

        if(ContextCompat.checkSelfPermission(WorkersInformationAssignmentCreate.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getDocuments();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void AddCompany(View view) {
        startActivity(new Intent(this, CompanyAdd.class));
    }

    public boolean isConnected(){
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
        date.setText(i2 + "/" + (i1+1) + "/" + i);
        dateSet = true;
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        time.setText(i + ":" + i1);
        timeSet = true;
    }


    MediaRecorder recorder;
    String AudiosName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRecording.m4a";

    public void VoiceRecorder(View view) {
        if (ContextCompat.checkSelfPermission(WorkersInformationAssignmentCreate.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(WorkersInformationAssignmentCreate.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                recorder.setAudioEncodingBitRate(16*44100);
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
            } catch (Exception exception) {
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
        if (!startedRecording && !startedListening) {
            try {
                player.setDataSource(AudiosName);
                player.prepare();
                player.start();
                startedListening = true;

                fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            } catch (Exception ex) {
                fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                recorder_text.setText("Kayıt Dinlemedi!");
                recorder_text.setTextColor(Color.RED);
                startedListening = false;
            }
        } else if (!startedRecording) {
            player.pause();
            player.stop();
            fab_listener.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            recorder_text.setTextColor(Color.BLACK);
            startedListening = false;
        } else {
            Toast.makeText(this, "Kayıt esnasında dinleme yapılamaz", Toast.LENGTH_SHORT).show();
            startedListening = false;
        }
    }
}