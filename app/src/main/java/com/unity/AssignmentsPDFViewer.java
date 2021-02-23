package com.unity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class AssignmentsPDFViewer extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    PDFView pdfView;
    Intent intent;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments_p_d_f_viewer);

        StorageReference storageReference = storage.getReference();
        pdfView = findViewById(R.id.pdf_viewer);
        intent = getIntent();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.document_downloading));
        progressDialog.setMessage(getString(R.string.plase_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        String child = String.valueOf(intent.getIntExtra("id", 0));
        storageReference.child("Assignments").child(child).getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            pdfView.fromBytes(bytes).load();
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
        });
    }
}