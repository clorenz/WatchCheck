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
package de.uhrenbastler.watchcheck.ui;

import java.io.FileNotFoundException;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.TabHost;
import de.uhrenbastler.watchcheck.FinActivity;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.ResultsActivity;
import de.uhrenbastler.watchcheck.SelectWatchActivity;
import de.uhrenbastler.watchcheck.data.Exporter;
import de.uhrenbastler.watchcheck.data.Importer;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * The main activity: Tab framework and menu
 * @author clorenz
 * @created on 14.10.2011
 */
public class MainActivity extends TabActivity {
	
	public static final String PREFERENCE_CURRENT_WATCH = "currentWatch";
	public static final String SPEC_SELECT = "select";
	public static final String SPEC_CHECK = "check";
	public static final String SPEC_RESULTS = "results";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
	    setContentView(R.layout.main);
	    
	    TabHost tabHost = getTabHost();
	    
	    tabHost.addTab(tabHost.newTabSpec(SPEC_SELECT).setIndicator(getString(R.string.selectWatch)).setContent(
	                    new Intent(this, SelectWatchActivity.class)));
	    tabHost.addTab(tabHost.newTabSpec(SPEC_CHECK).setIndicator(getString(R.string.checkWatch)).setContent(
                new Intent(this, CheckWatchActivity.class)));
	    tabHost.addTab(tabHost.newTabSpec(SPEC_RESULTS).setIndicator(getString(R.string.results)).setContent(
                new Intent(this, ResultsActivity.class)));
	}
	
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refreshTabs();
	}


	public void setKeepScreenOn() {
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	
	public void releaseKeepScreenOn() {
		getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
	}


	public void refreshTabs() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int selectedWatchId = WatchCheckDBHelper.validateWatchId(this,preferences.getInt(PREFERENCE_CURRENT_WATCH, -1));
		
		Logger.debug("Selected Watch ID from preferences = "+selectedWatchId);
		
		TabHost tabHost = getTabHost();
	    // If no watch selected, disable the other tabs
	    if ( selectedWatchId < 0) {
	    	tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);	
	    	tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
	    	tabHost.setCurrentTab(0);
	    } else {
	    	tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(true);	
	    	// Bring "measure" tab into front
	    	tabHost.setCurrentTab(1);
	    	if ( !WatchCheckDBHelper.resultsAvailableForCurrentWatch(this,selectedWatchId))
	    		tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
	    	else
	    		tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);
	    }
	}
	
	
	public void displayResultTab() {
		TabHost tabHost = getTabHost();
		tabHost.setCurrentTab(2);
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
        case R.id.menuExportData:
        	try {
				String filename = new Exporter().export(this);
				new AlertDialog.Builder(this).setTitle(this.getString(R.string.dataExported))
					.setMessage(filename)
					.setCancelable(true)
					.setPositiveButton(this.getString(android.R.string.ok), null).create().show();
			} catch (Exception e) {
				Logger.error(e.getMessage(),e);
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
									Logger.error(e.getMessage(),e);
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
