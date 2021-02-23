package com.unity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;

public class Documents extends AppCompatActivity {
    WebView webView;
    ProgressDialog progressDialog;
    FloatingActionButton fab;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        webView = findViewById(R.id.web);
        fab = findViewById(R.id.fab);

        webView.getSettings().setJavaScriptEnabled(true);

        progressDialog = new ProgressDialog(Documents.this);
        progressDialog.setTitle(getString(R.string.document_loading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        String url = "https://www.unityotomasyon.com.tr/images/Dokuman/evrensel-gostergeuug-540-0296.pdf";
        String finalUrl = "https://docs.google.com/gview?embedded=true&url=" + url;
        webView.loadUrl(finalUrl);

        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url){
                progressDialog.dismiss();
            }
        });

        fab.setOnClickListener(view -> {
            if(IsConeected()){
                progressDialog.show();
                webView.loadUrl(finalUrl);
                webView.setWebViewClient(new WebViewClient() {

                    public void onPageFinished(WebView view, String url){
                        progressDialog.dismiss();
                    }
                });
            }
            else
                Toast.makeText(this, getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
        });
    }

    private Boolean IsConeected(){
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}