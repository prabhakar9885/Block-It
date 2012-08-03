package com.BlockIt;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Help_html extends Activity {

	WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_html);
		
		webView=(WebView) findViewById(R.id.help_html);
		WebSettings settings = webView.getSettings();
	    settings.setJavaScriptEnabled(true);

	    webView.loadUrl("file:///android_asset/Help.html");
	}
}
