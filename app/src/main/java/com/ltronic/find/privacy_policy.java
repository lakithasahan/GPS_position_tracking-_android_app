package com.ltronic.find;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class privacy_policy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        WebView wv = findViewById(R.id.web);
        wv.loadUrl("file:///android_asset/privacy_policy.html");
    }
}
