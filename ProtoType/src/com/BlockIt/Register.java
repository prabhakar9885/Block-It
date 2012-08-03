package com.BlockIt;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class Register extends Activity {

	TextView link;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		link = (TextView) findViewById(R.id.link);
		link.setText(Html.fromHtml("<a href=\"http://www.google.com\">register now</a>"));
        link.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
