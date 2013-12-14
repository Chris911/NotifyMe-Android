package com.NotifyMe;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewer extends Activity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(this.getIntent().getDataString());
    }
}