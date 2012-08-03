
/*
 * 	1.	This class adds a new contact to the database and sets its MIMETYPE
 * 		to "Blocked".
 * 	2.	The contacts that are added to the database by this class are 
 * 		deleted when, the user removes the contact from the "Calls blacklist".
 * 	3.	The contacts that are added by this class won't get synchronized with
 * 		the user contacts that are present in his Gmail account.
 * 	4.	The method "AddContactToBlock/onClick" return the data, pertaining to
 * 		the newly added contact, to the calling Activity 
 * 			i.e., "BlockCallsPressed/onActivityResult" 
 * 
 */

package com.BlockIt.calls;

import java.util.ArrayList;

import com.BlockIt.R;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AddContactToBlock extends Activity implements OnClickListener {

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("UI", "1");
		setContentView(R.layout.new_contact);
		Log.i("UI", "2");

		Log.i("Listener", "1");
		((Button) findViewById(R.id.Add)).setOnClickListener(this);
	}

	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		String name = ((EditText) findViewById(R.id.Name)).getText().toString();
		String number = ((EditText) findViewById(R.id.Number)).getText()
				.toString();

		if(name.equals("") || number.equals("")){
			Toast.makeText(getApplicationContext(), "Please fill up both the fields", Toast.LENGTH_LONG);
			return;
		}
		
		addingInProgress(name,number,"Blocked");
	}



	public void addingInProgress(String name, String number, String label) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = ops.size();

		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, null)
				.withValue(RawContacts.ACCOUNT_NAME, null).build());

		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						name).build());

		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				.withValue(Phone.NUMBER, number)
				.withValue(Phone.TYPE, Phone.TYPE_CUSTOM)
				.withValue(Phone.LABEL, label).build());

		try {
			ContentProviderResult[] res = getContentResolver().applyBatch(
					ContactsContract.AUTHORITY, ops);

			if (res != null && res[0] != null) {
				Uri newContactUri = res[0].uri;
				// 02-20 22:21:09 URI added
				// contact:content://com.android.contacts/raw_contacts/612
				Log.d("Success", "URI added contact:" + newContactUri);
//				Toast.makeText(getApplicationContext(),
//						name + ": " + number + " \n" + "Blocked",
//						Toast.LENGTH_LONG).show();

				Intent intent = new Intent();
				// add "returnKey" as a key and assign it the value
				intent.putExtra("ContactURI", newContactUri.toString());
				// get ready to send the result back to the caller
				// (MainActivity)
				// and put our intent into it (RESULT_OK will tell the caller
				// that
				// we have successfully accomplished our task..
				Log.d("Success","1");
				setResult(RESULT_OK, intent);
				// close this Activity...
				Log.d("Success","2");
				finish();
				Log.d("Success","3");
			} else
				Log.e("Failure", "Contact not added.");
		} catch (Exception e) {
			Log.e("Exception", "Contact not added.");
		}		
	}
}
