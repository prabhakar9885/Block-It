package com.BlockIt.sms;


import java.util.Locale;

import com.BlockIt.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddContactToBlockSMS extends Activity implements OnClickListener {

	Button add;
	String name, number;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_contact);
        
        add=(Button) findViewById(R.id.Add);
        add.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {

		name = ((EditText) findViewById(R.id.Name)).getText().toString();
		number = ((EditText) findViewById(R.id.Number)).getText().toString();

		AlertDialog.Builder builder = new AlertDialog.Builder(
				this);
		builder.setTitle("Confirm");
		builder.setMessage("Add to Blacklist: " + name + " ?");
		Log.d("BlockingCode", "2");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d("BlockingCode", "Confirmed");
				addToSMSBlacklist(name,number);
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d("BlockingCode", "Not Confirmed");
				return;
			}
		});
		
		builder.show();
	}

	protected void addToSMSBlacklist(String name2, String number2) {
		if (name.length() == 0 || number.length() == 0) {
			Toast.makeText(getApplicationContext(),
					"Please fill up both the fields", Toast.LENGTH_LONG);
			return;
		}
		
		SQLiteDatabase db;
		db = openOrCreateDatabase(
				"/data/data/com.BlockIt/databases/BlackListDB",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		db.setVersion(1);
		db.setLocale(Locale.getDefault());
		db.setLockingEnabled(true);
		db.execSQL("create table IF NOT EXISTS SMS_BlackList(names varchar(20) UNIQUE, numbers varchat(20))");

		// Insert the "PhoneNumbers" into database-table, "SMS_BlackList"
		ContentValues values = new ContentValues();
		values.put("names", name);
		values.put("numbers", number);
		if (db.insert("SMS_BlackList", null, values) == -1){
			Log.d("addToSMS_BlackList", "3: blockingCodeForSMS ");
			Toast.makeText(
					getApplicationContext(),
					name
							+ " already exist in database\n Please try a new name!!",
					Toast.LENGTH_LONG).show();
			db.close();
			return;
		}
		Log.d("addToSMS_BlackList", "4: blockingCodeForSMS ");

		Log.d("addToSMS_BlackList", "5: blockingCodeForSMS ");

		db.close();
		Toast.makeText(getApplicationContext(), name+" added to SMS blacklist", Toast.LENGTH_LONG).show();
		finish();		
	}
}
