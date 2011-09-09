/* ------------------------------------------------------------------------- *
$Source:$
$Author:$
$Date: $
$Revision: $

(C) 2006 Christoph Lorenz, <mail@christophlorenz.de>
All rights reserved.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ** ------------------------------------------------------------------------- */
package de.uhrenbastler.watchcheck;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import de.uhrenbastler.watchcheck.data.Watch.Watches;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @see http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
 * TODO Please remind clorenz to comment SelectWatchActivity.java
 * @author clorenz
 * @created on 08.09.2011
 */
public class SelectWatchActivity extends Activity {

	List<WatchItem> watches = new ArrayList<WatchItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.selectwatch);

		getAllWatchesFromDatabase();
		
		ListView listView = (ListView) findViewById(R.id.selectWatchListView);
		
		ListAdapter listAdapter = new WatchAdapter(this, R.layout.watch_row, watches);
		
		listView.setAdapter(listAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if ( id == (watches.size()-1)) {
					// "Add Watch"
					Intent intent = new Intent(SelectWatchActivity.this, AddWatchActivity.class);
	                startActivity(intent);
				} else {
					// Put found watch into preferences and enable tabs
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SelectWatchActivity.this);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(MainActivity.PREFERENCE_CURRENT_WATCH, (int)id);
					editor.commit();
					
					TabHost tabHost = ((TabActivity) getParent()).getTabHost();
					tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(true);	
			    	tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);
			    	
			    	// Bring "check" tab to front
			    	tabHost.setCurrentTab(1);
				}
			}
		});
	}
	
	
	

	// ->
	// http://www.androidcompetencycenter.com/2009/01/basics-of-android-part-iv-android-content-providers/
	private void getAllWatchesFromDatabase() {
		Uri uriWatches = Watches.CONTENT_URI;
		String[] columns = new String[] { Watches.WATCH_ID, Watches.NAME,
				Watches.SERIAL };

		Cursor cur = managedQuery(uriWatches, columns, null, null, Watches.NAME);

		if (cur.moveToFirst()) {
			Long id = null;
			String name = null;
			String serial = null;
			do {
				id = cur.getLong(cur.getColumnIndex(Watches.WATCH_ID));
				name = cur.getString(cur.getColumnIndex(Watches.NAME));
				serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));

				Log.d("WatchCheck", "Found watch with id=" + id + ", name="
						+ name + ", serial=" + serial);

				watches.add(new WatchItem(id,name, serial));
			} while (cur.moveToNext());
		}

		watches.add(new WatchItem(-1,"",getResources().getString(R.string.addWatch)));
	}
	
	
	private class WatchItem {
		long id;
		String name;
		String serial;
		
		public WatchItem(long id, String name, String serial) {
			this.id = id;
			this.name = name;
			this.serial = serial;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the serial
		 */
		public String getSerial() {
			return serial;
		}
		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}
	}
	
	
	private class WatchAdapter extends ArrayAdapter<WatchItem> {
		
		private List<WatchItem> watches;

		public WatchAdapter(Context context, int textViewResourceId,
				List<WatchItem> watches) {
			super(context, textViewResourceId, watches);
			this.watches = watches;
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.watch_row, null);
                }
                WatchItem w = watches.get(position);
                if (w != null) {
                        TextView name = (TextView) v.findViewById(R.id.watchName);
                        TextView serial = (TextView) v.findViewById(R.id.watchSerial);
                        if (name != null) {
                              name.setText(w.getName());                            }
                        if(serial != null){
                              serial.setText(w.getSerial());
                        }
                }
                return v;
        }
		
	}

}
