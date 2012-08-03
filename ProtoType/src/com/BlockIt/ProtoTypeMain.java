package com.BlockIt;

import com.BlockIt.calls.BlockCallsPressed;
import com.BlockIt.sms.BlockSMSPressed;
import com.BlockIt.R;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ProtoTypeMain extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	Button about;
	Button bCalls;
	Button bSMS;
	Button callRec;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
        /*AdView adView = new AdView(this, AdSize.BANNER, "a14dcc26fa42001");
        LinearLayout layout = (LinearLayout)findViewById(R.id.Adds);
        layout.addView(adView);
        adView.loadAd(new AdRequest());*/
        
		
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		Typeface face=Typeface.createFromAsset(getAssets(),"BRUSHSCI.TTF");
		tv1.setTypeface(face);

		bCalls = (Button) findViewById(R.id.bcalls);
		bSMS = (Button) findViewById(R.id.bsms);
		about = (Button) findViewById(R.id.help);
		callRec = (Button) findViewById(R.id.Exit);

		bCalls.setOnClickListener(this);
		bSMS.setOnClickListener(this);
		about.setOnClickListener(this);
		callRec.setOnClickListener(this);
		
		//Creating Database and tables
		SQLiteDatabase db = openOrCreateDatabase("/data/data/com.BlockIt/databases/BlackListDB",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		db.setVersion(1);
		//db.setLocale(Locale.getDefault());
		db.setLockingEnabled(true);
		db.close();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		switch (arg0.getId()) {
		case R.id.help: 		//About button pressed
			startActivity(new Intent(this, Help_html.class));
			break;
		case R.id.bcalls: 		//"Block Calls" button pressed
			startActivity(new Intent(this, BlockCallsPressed.class));
			break;
		case R.id.bsms: 		//"Block SMSs" button pressed
			startActivity(new Intent(this, BlockSMSPressed.class));
			break;
		case R.id.Exit:
			finish();
		}
	}

	/*
	// -----------------------------------------------------------------------
	// Options Menu:: ( Settings, Register )
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			break;
		case R.id.register:
			startActivity(new Intent(this, Register.class));
			break;
		}
		return true;
	}
	// Options Menu:: ( Settings, Register )
	 */
	
	
	// -----------------------------------------------------------------------

	/*
	 * Translate translator = new Translate();
	 * 
	 * String txt = (String) langu1.getText();
	 * 
	 * try { langu1.setText(Translate.execute("Bonjour le monde",
	 * Language.FRENCH, Language.ENGLISH)); } catch (Exception e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 */

}
