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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SelectWatchActivity.class);
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("select").setIndicator("Select watch",
	                      res.getDrawable(R.drawable.icon))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, WatchCheckActivity.class);
	    spec = tabHost.newTabSpec("check").setIndicator("Check watch",
	                      res.getDrawable(R.drawable.icon))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, WatchCheckActivity.class);
	    spec = tabHost.newTabSpec("results").setIndicator("Results",
	                      res.getDrawable(R.drawable.icon))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	    
	    tabHost.getTabWidget().getChildTabViewAt(1).setEnabled(false);	
	    tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);	
	}
}
