
/* This activity is called when the user selects the option "/main/Block calls"
 * 
 * The Activity displays three options(that can be performed on 
 * "Calls Blacklist") to the user.
 * 		1. Add from contacts
 * 		2. Add new Contact
 * 		3. View Blacklist
 */


package com.BlockIt.sms;

import java.util.Locale;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class BlockSMSPressed extends ListActivity implements OnItemClickListener{

	String[] list = { "From Contacts", "New Contact", "View Black-list" };
	static final int BLOCK_FROM_CONTACTS = 1337;
	static final int BLOCK_FROM_CONTACTS_AFTER_ADDING = 1338;
	SQLiteDatabase db;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);

		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
	}
	
		
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg2) {
		case 0: // Open Contacts
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					android.provider.ContactsContract.Contacts.CONTENT_URI),
					BLOCK_FROM_CONTACTS);
			break;
		case 1: // Adding a contact
			startActivity(new Intent(this, AddContactToBlockSMS.class));
		    break;
		case 2: // View Black-list
			startActivity(new Intent(this, ViewBlackListSMS.class));
		}		
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		if (resultCode == RESULT_CANCELED)
			return;
		
		switch (reqCode) {
		case BLOCK_FROM_CONTACTS:
			Log.d("BLockSMSPressed", "1: onActivityResult/Switch");
			blockingCodeForSMS(data.getData());
			break;
		}
	}


	private void blockingCodeForSMS(Uri result) {
		StringBuilder sb = new StringBuilder();

		final Cursor c1 = managedQuery(result, null, null, null, null);
		Log.d("BLockSMSPressed", "1: blockingCodeForSMS ");
		Log.i("BLockSMSPressed", c1.toString() + ": " + c1.moveToFirst() + ": "
				+ c1.getCount());
		final String name = c1.getString(c1
				.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
				
		//Display Confirmation box.
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this);
		builder.setTitle("Confirm");
		builder.setMessage("Add to Blacklist: " + name + " ?");
		Log.d("BlockingCode", "2");
		builder.setPositiveButton("Yes", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d("BlockingCode", "Confirmed");
				addToSMSBlacklist(name,c1);
			}
		});
		builder.setNegativeButton("No", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d("BlockingCode", "Not Confirmed");
				return;
			}
		});
		builder.show();
		
		
		
	}


	protected void addToSMSBlacklist(String name, Cursor c1) {
		Log.d("BLockSMSPressed", "2: blockingCodeForSMS ");

		//Create database and table, If they does'nt exist 
		db = openOrCreateDatabase("/data/data/com.BlockIt/databases/BlackListDB",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		db.setVersion(1);
		db.setLocale(Locale.getDefault());
		db.setLockingEnabled(true);
		db.execSQL("create table IF NOT EXISTS SMS_BlackList(names varchar(20) UNIQUE, numbers varchat(20))");
		
		//Insert the "PhoneNumbers" into database-table, "SMS_BlackList"
		ContentValues values=new ContentValues();
		values.put("names", name);
		
		
		final String lookupKey=c1.getString(c1
				.getColumnIndexOrThrow(Contacts.LOOKUP_KEY));
		String _ID=c1.getString(c1
				.getColumnIndexOrThrow(Contacts._ID));
		Log.d("BLockSMSPressed", _ID);
		c1.close();
		c1=getContentResolver().query(Data.CONTENT_URI, null, Data.CONTACT_ID + "=?" + " AND "
                + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                new String[] {String.valueOf(_ID)}, null);

		Log.d("BLockSMSPressed",c1.moveToFirst()+": "+c1.getCount());
		if(c1.getCount()==0){
			Toast.makeText(getApplicationContext(),
					"Selected contact has no contact number",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(getApplicationContext(), name+" added to SMS blacklist", Toast.LENGTH_LONG).show();

		String number=c1.getString(c1
				.getColumnIndexOrThrow(Phone.NUMBER));
		Log.d("BLockSMSPressed", number);
		values.put("numbers",number );
		if( db.insert("SMS_BlackList", null, values) == -1)
			Log.d("BLockSMSPressed", "3: blockingCodeForSMS ");
		Log.d("BLockSMSPressed", "4: blockingCodeForSMS ");
		
		Log.d("BLockSMSPressed", "5: blockingCodeForSMS ");
		
		db.close();
		c1.close();		
	}
	
	/*private String getNumber(String _ID) {
		Uri lookupUri = Uri.withAppendedPath(
				ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
		Uri res = ContactsContract.Contacts.lookupContact(getContentResolver(),
				lookupUri);
		Cursor c=managedQuery(Contacts.CONTENT_URI, null, null, null, null);
		return number;

	}*/
}
