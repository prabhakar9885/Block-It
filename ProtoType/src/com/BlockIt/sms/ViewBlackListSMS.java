package com.BlockIt.sms;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class ViewBlackListSMS extends ListActivity  implements OnItemLongClickListener {

	ArrayList<String> numbersList=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cursor c;
		SQLiteDatabase db;
		
		try {
			Log.i("ViewBlackListSMS", "onCreate 1");
			db = SQLiteDatabase.openDatabase(
					"/data/data/com.BlockIt/databases/BlackListDB", null,
					SQLiteDatabase.OPEN_READWRITE);
			
			Log.i("ViewBlackListSMS", "onCreate 2");
			c = db.query("SMS_BlackList", null, null, null, null, null,
					null);

			if (!c.moveToFirst()) {
				Log.i("ViewBlackListSMS", "c.getCount(): " + c.getCount());
				Toast.makeText(getApplicationContext(),
						"BlackList Database is empty!!", Toast.LENGTH_SHORT)
						.show();
				c.close();
				db.close();
				return;
			}
			ArrayList<String> blackList = new ArrayList<String>();

			do {
				Log.i("ViewBlackListSMS", "onCreate: " + c.getString(0) + ": "
						+ c.getString(1));
				blackList.add(c.getString(0));
				numbersList.add(c.getString(1));
			} while (c.moveToNext());

			Log.i("ViewBlackListSMS", "onCreate 3");
			adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blackList);
			Log.i("ViewBlackListSMS", "onCreate 4");
			setListAdapter(adapter);
			Log.i("ViewBlackListSMS", "onCreate 5");
			getListView().setOnItemLongClickListener(this);
			Log.i("ViewBlackListSMS", "onCreate 6");
			c.close();
			db.close();

		} catch (SQLException e) {
			Log.i("ViewBlackListSMS", "BlackList Database not found!!");
			Toast.makeText(getApplicationContext(),
					"BlackList Database not found!!", Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		
		int position=arg2;
		final String selectedItem = (String) arg0.getItemAtPosition(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm");
		if (numbersList.get(position) == null)
			builder.setMessage("Are you sure, you want to delete "
					+ selectedItem + "?");
		else
			builder.setMessage("Are you sure, you want to delete "
					+ selectedItem + ": " + numbersList.get(position) + "?");

		builder.setPositiveButton("Yes", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SQLiteDatabase db = SQLiteDatabase.openDatabase(
						"/data/data/com.BlockIt/databases/BlackListDB", null,
						SQLiteDatabase.OPEN_READWRITE);
				
				Log.i("ViewBlackListSMS", "onItemLongClick Delete STARTED");
				db.delete("SMS_BlackList", "names=?",
						new String[] { selectedItem });
				Log.i("ViewBlackListSMS", "onItemLongClick Delete ENDED");
			
				adapter.remove(selectedItem);
				adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),
						selectedItem + " deleted from black-list",
						Toast.LENGTH_SHORT).show();
				db.close();
			}
		});
		
		builder.setNegativeButton("No", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		builder.show();
		return false;
	}
}
