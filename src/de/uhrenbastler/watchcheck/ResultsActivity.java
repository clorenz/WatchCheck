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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.uhrenbastler.watchcheck.data.Log;
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.data.Watch.Watches;

public class ResultsActivity extends Activity {
	
	// TODO: Das muß eine List of Lists werden
	// TODO: Oder sogar eine List of List of Lists (wenn nach Position unterschieden werden soll)
	List<Log> results = new ArrayList<Log>();
	
	ListView listView;
	
	
	/**
	 * Since onResume() is called after onCreate, we must here
	 * initialize the listAdapter and populate it with data!
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		populateListAdapter();
	}
	
	
	private void populateListAdapter() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
        
		getLogsForCurrentWatchFromDatabase(selectedWatchId);
		
		ListAdapter listAdapter = new ResultsAdapter(this, R.layout.result_row, results);
		
		listView.setAdapter(listAdapter);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.result);

		listView = (ListView) findViewById(R.id.resultListView);
	}
	
	
	
	private void getLogsForCurrentWatchFromDatabase(int currentWatchId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		results.clear();
		
		Uri uriLogs = Logs.CONTENT_URI;
		String[] columns = new String[] { Logs._ID, Logs.WATCH_ID, Logs.LOCAL_TIMESTAMP,
				Logs.DEVIATION};

		Cursor cur=null;
		try {
			cur = managedQuery(uriLogs, columns, Logs.WATCH_ID+"="+currentWatchId, null, Logs.LOCAL_TIMESTAMP+" asc");

			if (cur.moveToFirst()) {
				do {
					Log log = new Log();
					log.setWatchId(currentWatchId);
					log.setDeviation(cur.getFloat(cur.getColumnIndex(Logs.DEVIATION)));
					
					try {
						GregorianCalendar localTimestamp = new GregorianCalendar();
						localTimestamp.setTime(dateFormat.parse(cur.getString(cur.getColumnIndex(Logs.LOCAL_TIMESTAMP))));
						log.setLocalTimestamp(localTimestamp);
						log.setFlagReset(Boolean.parseBoolean(cur.getString(cur.getColumnIndex(Logs.FLAG_RESET))));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						android.util.Log.e("WatchCheck",e.getMessage());
					}

					android.util.Log.d("WatchCheck", "Found log: "+log);
	
					results.add(log);
				} while (cur.moveToNext());
			}

		} finally {
			if ( cur !=null )
				cur.close();
		}
	}
	
	
	private class ResultsAdapter extends ArrayAdapter<Log> {
		
		private List<Log> logs;

		public ResultsAdapter(Context context, int textViewResourceId,
				List<Log> logs) {
			super(context, textViewResourceId, logs);
			this.logs = logs;
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.result_row, null);
                }
                Log l = logs.get(position);
                TextView name=null;
                
                if (l != null) {
                        name = (TextView) v.findViewById(R.id.textViewResultTime);
                        if (name != null) {
                              name.setText(l.getLocalTimestamp().getTime().toString()+"="+l.getDeviation());
                              name.setGravity(Gravity.LEFT);
                        }
                }
                
                return v;
        }
		
	}

}
