/*
 * 	1.	This class displays the Contacts that are added to the VoiceMail
 * 	2.	It provides the user a choice of deleting the contact from the 
 * 		VoiceMail. (For this user must hold the desired contact for 2 seconds)
 * 	3.	If, the contact is added to the VoiceMail from the already existing 
 * 			contacts i.e., if MIMETYPE!="Blocked",
 * 				The contact will be removed from the VoiceMail.
 * 		else ( the contact is added to the database by the app. 
 * 			i.e., MIMETYPE=="Blocked",
 * 				The contact will be removed from the Contact.
 */

package com.BlockIt.calls;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class ViewBlackList extends ListActivity {

	ArrayList<String> blackListNames = new ArrayList<String>();
	ArrayList<String> blackListIds = new ArrayList<String>();
	ArrayList<String> blackListLookupKeys = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	private final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display BlackList UI
		fillUIList();

		OnItemLongClickListener longClickListener = new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				final int position = arg2;
				final String selectedItem = (String) arg0
						.getItemAtPosition(position);
				
				final ArrayList<ArrayList<String>> conflictingContacts = getConflictingContactsFromBlacklist(
						blackListNames, blackListLookupKeys, blackListIds,
						position);
				final ArrayList<String> conflictingNames = conflictingContacts
						.get(0);
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Confirm");
				builder.setMessage("Un-block: "
						+ conflictingNames.toString() + " ?");

				builder.setPositiveButton("Yes", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						/*
						 * ContentValues values1 = new ContentValues();
						 * values1.put(Contacts.SEND_TO_VOICEMAIL, "0"); Integer
						 * ii=getContentResolver().update(Contacts.CONTENT_URI,
						 * values1, Contacts.LOOKUP_KEY + "=?", new String[] {
						 * blackListLookupKeys.get(position) }); Log.d("****",
						 * "Rows updated: "+ii.toString());
						 */

						ArrayList<String> conflictingLookupKeys = conflictingContacts
								.get(1);
						ArrayList<String> conflictingIds = conflictingContacts
								.get(2);
						unBlockingCallsInProgress(conflictingNames,
								conflictingLookupKeys);

						for (String blackListId : conflictingIds)
							deleteManuallyAddedContact(blackListId);

						// Recreate the UI List.
						// Reinitializes the BlackList Variables.
						fillUIList();

						// adapter.remove(selectedItem);
						// adapter.notifyDataSetChanged();

						Toast.makeText(context, selectedItem + " deleted",
								Toast.LENGTH_SHORT).show();

					}

					/*
					 * This method accepts the BlacklistContacts and returns an
					 * ArrayList that contains the values that are not present
					 * in the conflicting ArrayList
					 */
					private ArrayList<String> updateThisList(
							ArrayList<String> blackList,
							ArrayList<String> conflictingList) {
						ArrayList<String> temp = new ArrayList<String>();
						for (String k : conflictingList) {
							if (!blackList.contains(k)) {
								temp.add(k);
							}
						}
						return temp;
					}

					/*
					 * Accepts a RawContactId. If that Id belongs to a manually
					 * added contact(Via., app) then, the contact will be
					 * deleted
					 */
					private void deleteManuallyAddedContact(String blackListId) {
						Cursor c1 = getContentResolver().query(
								ContactsContract.Data.CONTENT_URI,
								null,
								ContactsContract.Data.RAW_CONTACT_ID + "=?"
										+ " AND "
										+ ContactsContract.Data.MIMETYPE + "='"
										+ Phone.CONTENT_ITEM_TYPE + "'",
								new String[] { blackListId }, null);
						c1.moveToFirst();

						String id1;
						try {
							Log.i("*******", "Check for the group \"Blocked\"");
							id1 = c1.getString(c1
									.getColumnIndex(Data.RAW_CONTACT_ID));
							Log.i("*******", "Checked: " + id1);
						} catch (Exception e) {
							adapter.remove(selectedItem);
							adapter.notifyDataSetChanged();
							Log.i("*******", "Checked: Exception");
							Toast.makeText(context, selectedItem + " deleted",
									Toast.LENGTH_SHORT).show();
							c1.close();
							return;
						}

						// If the contact selected is NOT ADDED by the BlockIT,
						// then don't delete the Contact
						if (c1.getString(c1.getColumnIndex(Data.DATA3)) == null) {
							c1.close();
						} else {
							// else, it means that the contact selected is ADDED
							// by the BlockIT.
							// So, delete the Contact
							Log.i("Deleted: ", "1");
							String id = blackListId;
							Log.i("Deleted: ", "2");
							ArrayList ops = new ArrayList();
							String[] args = new String[] { id };
							Log.i("Deleted: ", "3");
							// if id is raw contact id
							ops.add(ContentProviderOperation
									.newDelete(RawContacts.CONTENT_URI)
									.withSelection(RawContacts._ID + "=?", args)
									.build());
							Log.i("Deleted: ", "4");
							try {
								getContentResolver().applyBatch(
										ContactsContract.AUTHORITY, ops);
								Log.i("Deleted: ", "5");
							} catch (Exception e) {
								Log.i("Deleted: ", "failed" + id);
								c1.close();
								e.printStackTrace();
							}
							c1.close();

						}

						Log.i("Deleted: ", "Successful " + id1);

					}

					/*
					 * It accepts the LookupKeys pertaining to the contacts (
					 * Selected contact + Conflicting contacts ) and adds them
					 * to the VoiceMail.
					 */
					private void unBlockingCallsInProgress(
							ArrayList<String> names,
							ArrayList<String> lookupKeys) {
						ContentValues values = new ContentValues();
						values.put(Contacts.SEND_TO_VOICEMAIL, "0");

						int i = 0;
						for (String lookupKey : lookupKeys) {
							Uri lookupUri = Uri
									.withAppendedPath(
											ContactsContract.Contacts.CONTENT_LOOKUP_URI,
											lookupKey);
							Uri res = ContactsContract.Contacts.lookupContact(
									getContentResolver(), lookupUri);
							adapter.remove(names.get(i++));
							Log.d("BlockingCode", names.get(i - 1));
							getContentResolver()
									.update(res, values, null, null);
						}
						adapter.notifyDataSetChanged();
					}

				});

				builder.setNegativeButton("No", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				builder.show();
				return false;
			}
		};
		getListView().setOnItemLongClickListener(longClickListener);
	}

	/*
	 * Accepts an ArrayList of LookupKeys and a LookupKey of a selected contact.
	 * Returns all the contact-numbers corresponding to that LookupKey
	 */
	private ArrayList<String> getContactNumbers(String selectedLookupKey) {
		Cursor c = managedQuery(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		c.moveToFirst();
		Log.d("Selected Numbers", c.getCount() + ": " + c.getColumnCount());

		ArrayList<String> numbersInSelectedContact = new ArrayList<String>();
		for (int i = 0; i < c.getCount(); i++) {
			if ((c.getString(c.getColumnIndex("lookup")))
					.equals(selectedLookupKey)) {
				String number = c
						.getString(c
								.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
				numbersInSelectedContact.add(number);
				Log.d("Selected Numbers", number);
			}
			c.moveToNext();
		}
		return numbersInSelectedContact;
	}

	/*
	 * Fetches the list of all the contacts that must be removed from the
	 * blacklist, in order to delete the selected contact from the blacklist
	 */
	private ArrayList<ArrayList<String>> getConflictingContactsFromBlacklist(
			ArrayList<String> blackListNames,
			ArrayList<String> blackListLookupKeys,
			ArrayList<String> blackListIds, int position) {
		String selectedLookupKey = blackListLookupKeys.get(position);
		Uri lookupUri = Uri
				.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
						selectedLookupKey);
		Uri resSelected = ContactsContract.Contacts.lookupContact(
				getContentResolver(), lookupUri);

		Log.d("getConflictingContactsFromBlacklist", resSelected.toString());

		// The ArrayList stores all the contact numbers that are
		// present under the contact that is selected by the user
		ArrayList<String> numbersInSelectedContact = getContactNumbers(selectedLookupKey);
		// The contact numbers, of the selected contact are retrieved.

		ArrayList<String> conflictingContactsLookups = new ArrayList<String>();
		ArrayList<String> conflictingContactsNames = new ArrayList<String>();
		ArrayList<String> conflictingContactIds = new ArrayList<String>();
		int i = 0;

		ProcessNextKey: for (String lookupKey : blackListLookupKeys) {
			ArrayList<String> numbers = getContactNumbers(lookupKey);
			String name = blackListNames.get(i);
			String id = blackListIds.get(i++);
			for (String number : numbers)
				if (numbersInSelectedContact.contains(number)) {
					conflictingContactsLookups.add(lookupKey);
					conflictingContactsNames.add(name);
					conflictingContactIds.add(id);
					Log.d("Conflicting names", name);
					continue ProcessNextKey;
				}
		}
		Log.d("getConflictingContactsFromBlacklist", "RETURN");

		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		list.add(conflictingContactsNames);
		list.add(conflictingContactsLookups);
		list.add(conflictingContactIds);
		return list;
	}

	/*
	 * Populates the BlackList UI with the contacts that are added to VoiceMail
	 * Reinitializes the BlackList Variables, blackListLookupKeys blackListIds
	 * blackListNames
	 */
	private void fillUIList() {
		final Cursor cursor = getContentResolver().query(Contacts.CONTENT_URI,
				null, Contacts.SEND_TO_VOICEMAIL + "=?", new String[] { "1" },
				null);
		String name;

		if (!cursor.moveToFirst()) {
			Log.i("ViewBlackListSMS", "c.getCount(): " + cursor.getCount());
			Toast.makeText(getApplicationContext(),
					"BlackList Database is empty!!", Toast.LENGTH_SHORT).show();
			cursor.close();
			return;
		}

		blackListLookupKeys.clear();
		blackListIds.clear();
		blackListNames.clear();
		do {
			Uri lookupUri = Contacts.getLookupUri(cursor.getLong(cursor
					.getColumnIndexOrThrow(RawContacts._ID)), cursor
					.getString(cursor.getColumnIndexOrThrow(Data.LOOKUP_KEY)));
			// blackListLookupKeys.add(lookupUri.toString());
			blackListLookupKeys.add(cursor.getString(cursor
					.getColumnIndexOrThrow(Data.LOOKUP_KEY)));
			Log.i("Black-List", lookupUri.toString());
			blackListIds.add(cursor.getString(cursor
					.getColumnIndexOrThrow(RawContacts._ID)));
			name = cursor.getString(cursor
					.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
			Log.i("Black-List", name);
			blackListNames.add(name);
		} while (cursor.moveToNext());
		cursor.close();
		Log.i("Black-List", "3");

		// Creating UI List
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, blackListNames);
		setListAdapter(adapter);

	}
}
