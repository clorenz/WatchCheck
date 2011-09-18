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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.data.Watch.Watches;

/**
 * @see http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
 * @author clorenz
 * @created on 08.09.2011
 */
public class SelectWatchActivity extends Activity {

	List<Watch> watches = new ArrayList<Watch>();
	ListView listView;
	long selectedWatchId;
	

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
		getAllWatchesFromDatabase();
		
		ListAdapter listAdapter = new WatchAdapter(this, R.layout.watch_row, watches);
		
		listView.setAdapter(listAdapter);
	}




	/**
	 * Only generic things here; no real data handling!
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.selectwatch);

		listView = (ListView) findViewById(R.id.selectWatchListView);

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
					editor.putInt(MainActivity.PREFERENCE_CURRENT_WATCH, (int)watches.get((int)id).getId());
					editor.commit();
					
					TabHost tabHost = ((TabActivity) getParent()).getTabHost();
					tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(true);	
			    	tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);
			    	
			    	// Bring "check" tab to front
			    	tabHost.setCurrentTab(1);
				}
			}
		});

		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				
				int position = ((AdapterContextMenuInfo)menuInfo).position;
				
				// If "add watch" is selected, we do not display the context menu!
				if ( position >= watches.size()-1)
					return;
				
				Watch watch = (Watch) listView.getAdapter().getItem(position);
				
				selectedWatchId = watch.getId();
				menu.setHeaderTitle(watch.getAsTitleString());
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.cm_select_watch, menu);
			}			
		});
	}
	
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		final Watch watch = (Watch) listView.getAdapter().getItem(menuInfo.position);
		
		switch (item.getItemId()) {
			case R.id.selectWatchEdit:
				Intent intent = new Intent(SelectWatchActivity.this, EditWatchActivity.class);
                intent.putExtra(Watches._ID, (int)watch.getId());				
                startActivity(intent);
				return true;
			case R.id.selectWatchDelete:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getResources().getString(R.string.deleteWatch).replace("%s",watch.getName()))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   getContentResolver().delete(Watches.CONTENT_URI, Watches.WATCH_ID+"="+watch.getId(), null);
				        	   dialog.dismiss();
				        	   populateListAdapter();
				           }
				       })
				       .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				
				alert.show();
				return true;
			default:
				Log.e("WatchCheck","unknown itemId "+item.getItemId());
				return false;
		}
	}




	// ->
	// http://www.androidcompetencycenter.com/2009/01/basics-of-android-part-iv-android-content-providers/
	private void getAllWatchesFromDatabase() {
		watches.clear();
		
		Uri uriWatches = Watches.CONTENT_URI;
		String[] columns = new String[] { Watches._ID, Watches.NAME,
				Watches.SERIAL, Watches.COMMENT };

		Cursor cur=null;
		try {
			cur = managedQuery(uriWatches, columns, null, null, Watches.NAME+" collate nocase");

			if (cur.moveToFirst()) {
				Long id = null;
				String name = null;
				String serial = null;
				String comment = null;
				do {
					id = cur.getLong(cur.getColumnIndex(Watches._ID));
					name = cur.getString(cur.getColumnIndex(Watches.NAME));
					serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));
					comment = cur.getString(cur.getColumnIndex(Watches.COMMENT));
	
					Log.d("WatchCheck", "Found watch with id=" + id + ", name="
							+ name + ", serial=" + serial);
	
					watches.add(new Watch(id,name,serial,comment));
				} while (cur.moveToNext());
			}

			watches.add(new Watch(-1,getResources().getString(R.string.addWatch),null,null));
		} finally {
			if ( cur !=null )
				cur.close();
		}
	}
	
	
	private class WatchAdapter extends ArrayAdapter<Watch> {
		
		private List<Watch> watches;

		public WatchAdapter(Context context, int textViewResourceId,
				List<Watch> watches) {
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
                Watch w = watches.get(position);
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
