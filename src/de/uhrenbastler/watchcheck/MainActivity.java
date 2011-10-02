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

import java.io.FileNotFoundException;

import de.uhrenbastler.watchcheck.data.ExportException;
import de.uhrenbastler.watchcheck.data.Exporter;
import de.uhrenbastler.watchcheck.data.Importer;
import de.uhrenbastler.watchcheck.data.WatchCheckLogContentProvider;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	
	public static final String PREFERENCE_CURRENT_WATCH = "currentWatch";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int selectedWatchId = preferences.getInt(PREFERENCE_CURRENT_WATCH, -1);
		
		Log.d("WatchCheck","Selected Watch ID from preferences = "+selectedWatchId);
		
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SelectWatchActivity.class);
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("select").setIndicator("Select watch", null)
	                      //res.getDrawable(R.drawable.icon))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, WatchCheckActivity.class);
	    spec = tabHost.newTabSpec("check").setIndicator("Check watch", null)
	                      //res.getDrawable(R.drawable.icon))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, ResultsActivity.class);
	    spec = tabHost.newTabSpec("results").setIndicator("Results", null)
	                      //res.getDrawable(R.drawable.icon))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // If no watch selected, disable the other tabs
	    if ( selectedWatchId < 0) {
	    	tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);	
	    	tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
	    	tabHost.setCurrentTab(0);
	    } else {
	    	// Bring "measure" tab into front
	    	tabHost.setCurrentTab(1);
	    }
	    
	    
	    //TODO: Idee: Hier NTP ermitteln und via http://stackoverflow.com/questions/820398/android-change-custom-title-view-at-run-time
	    // oder noch besser http://www.londatiga.net/how-to-create-custom-window-title-in-android/
	    // einen Marker in die Titelbar setzen
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menuAbout: 
        	// Show "about" dialogue
        	PackageInfo pInfo=null;
			try {
				pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {}
        	new AlertDialog.Builder(this).setTitle(this.getString(R.string.app_name)+
        			"\nVersion: "+(pInfo!=null?pInfo.versionName:"unknown"))
        		.setCancelable(true).setIcon(R.drawable.watchcheck)
        		.setMessage(this.getString(R.string.app_about))
        		.setPositiveButton(this.getString(android.R.string.ok), null).create().show();
        	return true;
        case R.id.menuSettings:
        	return true;
        case R.id.menuExportData:
        	try {
				String filename = new Exporter().export(this);
				new AlertDialog.Builder(this).setTitle(this.getString(R.string.dataExported))
					.setMessage(filename)
					.setCancelable(true)
					.setPositiveButton(this.getString(android.R.string.ok), null).create().show();
			} catch (Exception e) {
				Log.e("WatchCheck",e.getMessage());
			}
        	return true;
        case R.id.menuImportData:
        		new AlertDialog.Builder(this).setTitle(this.getString(R.string.warning))
        		.setMessage(this.getString(R.string.reloadDatabase))
        		.setCancelable(false)
        		.setPositiveButton(this.getString(android.R.string.yes), 
        				new DialogInterface.OnClickListener() {
        	        		public void onClick(DialogInterface dialog, int which) {
        	        			try {
									new Importer().doImport(MainActivity.this);									
									Intent intent = new Intent(MainActivity.this, FinActivity.class).
										setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
									finish();
									startActivity(intent);
								} catch (FileNotFoundException e) {
									Log.e("WatchCheck", e.getMessage());
								}
        	        		}
        	    		})
        		.setNegativeButton(this.getString(android.R.string.no), null)
        		.create().show();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
