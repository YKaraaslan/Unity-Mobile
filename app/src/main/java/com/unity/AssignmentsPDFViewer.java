package com.unity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        }).addOnFailureListener(e -> progressDialog.dismiss());
    }
}