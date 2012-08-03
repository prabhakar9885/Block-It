
/* This activity is called when the user selects the option "/main/Block calls"
 * 
 * The Activity displays three options(that can be performed on 
 * "Calls Blacklist") to the user.
 * 		1. Add from contacts
 * 		2. Add new Contact
 * 		3. View Blacklist
 */

package com.BlockIt.calls;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class BlockCallsPressed extends ListActivity implements
		OnItemClickListener {

	static final int BLOCK_FROM_CONTACTS = 1337;
	static final int BLOCK_FROM_CONTACTS_AFTER_ADDING = 1338;
	Uri contactData = null;
	String[] list = { "From Contacts", "New Contact", "View Black-list" };
	ArrayList<String> lst = new ArrayList<String>(Arrays.asList(list));

	
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);

		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
	}

	
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg2) {
		case 0: // Open Contacts
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					android.provider.ContactsContract.Contacts.CONTENT_URI),
					BLOCK_FROM_CONTACTS);
			break;
		case 1: // Adding a contact
			startActivityForResult(new Intent(this, AddContactToBlock.class),
					BLOCK_FROM_CONTACTS_AFTER_ADDING);
			break;
		case 2: // View Black-list
			startActivity(new Intent(this, ViewBlackList.class));
		}
	}

	
	
	
	/*
	 * This method adds the contact pertaining to a given "LookupUri" to the
	 * voice-mail
	 */
	Boolean isConfirmed;
	private void blockingCodeForCalls(Uri result) {

		final StringBuilder sb = new StringBuilder();

		String id = result.getLastPathSegment();
		Log.i("RESULT_OK", "LookupUri: " + result);
		Log.i("RESULT_OK", "Path: " + result.getPath());
		Log.i("RESULT_OK", "LastPathSegment: " + id);
		
		
		//Finding Contact._id of the selected contact
		Cursor c = managedQuery(result, null, null, null, null);
		c.moveToFirst();
		String _id=c.getString(c.getColumnIndex("_id"));//Contact._id
		
		c = managedQuery(Contacts.CONTENT_URI, null,Contacts._ID +"=?", new String[]{_id}, null);
		c.moveToFirst();
		int i=0;
		while(i<c.getColumnCount()){
			Log.i("Info**",c.getColumnName(i)+": "+c.getString(i));
			i++;
		}
		String lookupKey=c.getString(c.getColumnIndex("lookup"));//lookupKey
		Log.i("RESULT_OK", "LookupKey: " + lookupKey);
		Log.i("RESULT_OK", "LookupKey: " + c.getString(c.getColumnIndex("display_name")));
		
		
		//Get Contacts that have at least one number, that is present in the selected contact
		ArrayList<ArrayList<String>> targetContacts = getConfictingContacts(lookupKey);
		if(targetContacts==null){
			Toast.makeText(getApplicationContext(),
					"No contact number present in selected Contact. Unable to block.",
					Toast.LENGTH_LONG).show();
			return;
		}
		final ArrayList<String> lookupKeys = targetContacts.get(2);
		final ArrayList<String> names = targetContacts.get(0);
		Log.d("BlockingCode", "1");
		//saveNonCoflictingNubers(targetContacts);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this);
		builder.setTitle("Confirm");
		builder.setMessage("Add to Blacklist: " + names.toString() + " ?");
		Log.d("BlockingCode", "2");
		builder.setPositiveButton("Yes", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				blockingCallsInProgress(lookupKeys);
				Log.d("BlockingCode", "9");
				Toast.makeText(getApplicationContext(), "Blacklisted: "+names.toString(),
						Toast.LENGTH_SHORT).show();
			}
		});
		Log.d("BlockingCode", "3");
		builder.setNegativeButton("No", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				
				
				// Delete the added contact, from phone-book ---Start
				ContentResolver cr = getContentResolver();
			    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
			            null, null, null, null);
			    while (cur.moveToNext()) {
			        try{
			            String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
			            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKeys.get(0));
			            System.out.println("The uri is " + uri.toString());
			            cr.delete(uri, null, null);
			        }
			        catch(Exception e)
			        {
			            System.out.println(e.getStackTrace());
			        }
			    }
				//  Delete the added contact, from phone-book ---Start
				
				
				Toast.makeText(getApplicationContext(),
						"Blacklisting canceled", Toast.LENGTH_SHORT).show();
				return;
			}
		});
		Log.d("BlockingCode", "4");
		builder.show();
		Log.d("BlockingCode", "5");
		
		//------blockingCallsInProgress(lookupKeys);
		
		// If cursor is not empty
/*		if (c.moveToFirst()) {
			String name = c
					.getString(c
							.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
			c.close();
			Log.d("BlockingCode", "2");
			/*String hasPhoneNumber = c
					.getString(c
							.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			*/Log.d("BlockingCode", "3");
/*			sb.append(name + " ");

			// If the contact has at least one phone number
			/*if (Integer.parseInt(hasPhoneNumber) > 0)*/ {
/*				Log.d("BlockingCode", "4");
				ContentValues values = new ContentValues();
				Log.d("BlockingCode", "5");
				values.put(Contacts.SEND_TO_VOICEMAIL, "1");
				Log.d("BlockingCode", "6");

				// Add contact to voice mail by setting the
				// SEND_TO_VOICEMAIL
				getContentResolver().update(Contacts.CONTENT_URI, values,
						Contacts._ID + "=?", new String[] { id });
				Log.d("BlockingCode", "7");

				sb.append(" is now blocked  ");
				Log.d("BlockingCode", "8");				
			} //else
				//sb.append("has no Phone number, to block!!");
*/			
	//---		Log.d("BlockingCode", "9");
//---			Toast.makeText(getApplicationContext(), sb.toString(),
//---					Toast.LENGTH_LONG).show();
		}
	}

	
	/*
	 * Unblocks the numbers that are not-present in the Selected contact and
	 * present in the conflicting contacts, by adding a duplicate copy of that
	 * number to the Contacts database
	 */
	private void saveNonCoflictingNubers(
			ArrayList<ArrayList<String>> targetContacts) {
		for (ArrayList<String> eachContact : targetContacts) {
			ArrayList<String> names=targetContacts.get(0);
			ArrayList<String> numbers=targetContacts.get(1);
			ArrayList<String> lookup=targetContacts.get(2);
			for (int i = 0; i < eachContact.size(); i++)
				new AddContactToBlock().addingInProgress(names.get(i),
						numbers.get(i), "xxx");
		}
	}




	/*
	 * It accepts the LookupKeys pertaining to the contacts 
	 * ( Selected contact + Conflicting contacts )
	 * and  adds them to the VoiceMail.
	 */
	private void blockingCallsInProgress(ArrayList<String> lookupKeys) {
		
		
		ContentValues values = new ContentValues();
		values.put(Contacts.SEND_TO_VOICEMAIL, "1");
		
		for (String lookupKey : lookupKeys) {
			Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
		    Uri res = ContactsContract.Contacts.lookupContact(getContentResolver(), lookupUri);
			Log.d("BlockingCode", lookupKey.toString());
			Log.d("BlockingCode", res.toString());
			getContentResolver().update(res, values, null, null);
		}
	}




	/*
	 *  Fetches all the contacts that are having at least one contact-number same as that of
	 *  the selected contact into the ArrayLists 
	 *  	matchingContactNames
	 *  	matchingContactNumbers
	 *  	matchingContactPhoneLookup
	 *  and then, returns their Collection as an ArrayList
	 */
	private ArrayList<ArrayList<String>> getConfictingContacts(String lookupKey) {
		//Get Contact number, of the selected contact.
		//Each no. will have a record in the path
		Cursor c2 = managedQuery(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				null,
				null, null);
		c2.moveToFirst();
		
		//The ArrayList stores all the contact numbers that are 
		//present under the contact that is selected by the user
		Log.d("Selected Numbers", c2.getCount() + ": " + c2.getColumnCount());
		if(c2.getCount()==0)
			return null;
		ArrayList<String> numbersInSelectedContact = new ArrayList<String>();
		for (int i = 0; i < c2.getCount(); i++) {
			if ((c2.getString(c2.getColumnIndex("lookup"))).equals(lookupKey)) {
				String number = c2
						.getString(c2
								.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
				numbersInSelectedContact.add(number);
				Log.d("Selected Numbers", number);
			}
			c2.moveToNext();
		}
		//The contact numbers, of the selected contact are retrieved.
		
		
		// Get the list of all the contacts, which have at least one contact
		// number as that of present in the selected contact
		ArrayList<String> matchingContactNames=new ArrayList<String>();
		ArrayList<String> matchingContactNumbers=new ArrayList<String>();
		ArrayList<String> matchingContactPhoneLookup=new ArrayList<String>();
		c2.moveToFirst();
		do {
			String phoneNumber = c2
					.getString(c2
							.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
			String phoneLookup = c2.getString(c2.getColumnIndex("lookup"));
			String phoneDisplayName = c2.getString(c2
					.getColumnIndex("display_name"));
			// Log.i("Info**", "Phone: " + phoneNumber);
			// Log.i("Info**", "Phone: " + phoneLookup);
			// Log.i("Info**", "Phone: " + phoneDisplayName);
			if (numbersInSelectedContact.contains(phoneNumber)) {
				Log.i("Info**", "Matching: " + phoneDisplayName + ": "
						+ phoneLookup);
				matchingContactNames.add(phoneDisplayName);
				matchingContactNumbers.add(phoneNumber);
				matchingContactPhoneLookup.add(phoneLookup);
			}
			Log.i("Info**", "*********Next Row**********");
		} while (c2.moveToNext());	
		
		ArrayList<ArrayList<String>> aggregate=new ArrayList<ArrayList<String>>();
		aggregate.add(matchingContactNames);
		aggregate.add(matchingContactNumbers);
		aggregate.add(matchingContactPhoneLookup);
		
		return aggregate;
	}




	/*
	 * When the user selects a contact from the already existing Contacts, the
	 * data pertaining to that contact is returned to the below method, for
	 * extracting the Lookup URI, pertaining to the selected contact.
	 * 
	 * Then, the LookUp URI is passed to the method
	 * "BlockCallsPressed/blockingCodeForCalls", for getting it added to the
	 * "Calls Blacklist".
	 */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		Log.d("sub-Menu", "1");
		if (resultCode == RESULT_CANCELED)
			return;

		Log.d("sub-Menu", "2");
		// Identify our request code
		switch (reqCode) {
		case BLOCK_FROM_CONTACTS_AFTER_ADDING:
			Log.d("sub-Menu", "1/1");
			Bundle extras = data.getExtras();
			
			if(extras!=null){
				Log.d("sub-Menu", "1/2");
				String uriAsString=extras.getString("ContactURI");
				Log.d("sub-Menu", "1/4");
				Uri uri = Uri.parse(uriAsString);
				Log.d("sub-Menu", "1/5");
				blockingCodeForCalls(uri);
				Log.d("sub-Menu", "1/6");
			}
			else
				Log.d("sub-Menu", "1/7        extras : null");
			break;
		case BLOCK_FROM_CONTACTS:
			Log.d("sub-Menu", "2/1");
			blockingCodeForCalls(data.getData());
			Log.d("sub-Menu", "2/2");
			break;
		}
	}
}
