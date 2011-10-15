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
package de.uhrenbastler.watchcheck.db;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import de.uhrenbastler.watchcheck.data.Log;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Watch.Watches;
import de.uhrenbastler.watchcheck.tools.Logger;

public class WatchCheckDBHelper {
	
	public static Watch getWatchFromDatabase(long id, ContentResolver cr) {
    	Uri selectedWatchUri = Uri.withAppendedPath(Watches.CONTENT_URI, ""+id);
    	
    	Logger.debug("Retrieving watch "+id+" from content provider with uri="+selectedWatchUri);
    	
    	String[] columns = new String[] { Watches._ID, Watches.NAME, Watches.SERIAL, Watches.COMMENT };
    	
    	Cursor cur = null;
    	
    	try {
    		cur = cr.query(selectedWatchUri, columns, Watches._ID+"="+id, null, null);
    	
			if (cur.moveToFirst()) {
				String name = null;
				String serial = null;
				String comment = null;
				do {
					id = cur.getLong(cur.getColumnIndex(Watches._ID));
					name = cur.getString(cur.getColumnIndex(Watches.NAME));
					serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));
					comment = cur.getString(cur.getColumnIndex(Watches.COMMENT));
					
					return new Watch(id, name, serial, comment);
	
				} while (cur.moveToNext());
			}
    	} finally {
    		if ( cur !=null)
    			cur.close();
    	}
			
		return null;
    }
	
	
	
	/**
	 * Verify, if there are results for the selected watch
	 * @param selectedWatchId
	 * @return
	 */
	public static boolean resultsAvailableForCurrentWatch(Activity activity, int selectedWatchId) {
		Uri uriLogs = Logs.CONTENT_URI;
		String[] columns = new String[] { Logs._ID, Logs.WATCH_ID };
		Cursor cur=null;
		try {
			cur = activity.managedQuery(uriLogs, columns, Logs.WATCH_ID+"="+selectedWatchId, null, Logs._ID);
			if (cur.moveToFirst()) {
				Logger.debug("Found results for watch "+selectedWatchId);
				return true;
			} else {
				Logger.debug("NO results for watch "+selectedWatchId);
				return false;
			}
		} finally {
			if ( cur !=null )
				cur.close();
		}
	}
	
	
	/**
	 * Verify, that a watch with the given watchId exists
	 * @param int1
	 * @return
	 */
	public static int validateWatchId(Activity activity, int watchIdToValidate) {

		Uri uriWatches = Watches.CONTENT_URI;
		String[] columns = new String[] { Watches._ID };
		Cursor cur=null;
		try {
			cur = activity.managedQuery(uriWatches, columns, Watches._ID+"="+watchIdToValidate, null, Watches._ID);
			if (cur.moveToFirst()) {
				return watchIdToValidate;
			}
		} finally {
			if ( cur !=null )
				cur.close();
		}
		
		Logger.warn("No watch with ID "+watchIdToValidate+" found in database!");
		return -1;
	}



	public static Log getLastLogOfWatch(Activity activity, int watchId) {
		Uri uriLogs = Logs.CONTENT_URI;
		String[] columns = new String[] { Logs._ID, Logs.WATCH_ID, Logs.DEVIATION, Logs.POSITION, Logs.TEMPERATURE };
		
		Cursor cur=null;
		try {
			cur = activity.managedQuery(uriLogs, columns, Logs.WATCH_ID+"="+watchId, null, Logs._ID+" DESC");
			if (cur.moveToFirst()) {
				Log log = new Log();
				log.setDeviation(cur.getDouble(cur.getColumnIndex(Logs.DEVIATION)));
				log.setPosition(cur.getString(cur.getColumnIndex(Logs.POSITION)));
				log.setTemperature(cur.getInt(cur.getColumnIndex(Logs.TEMPERATURE)));
				return log;
			} else {
				return null;
			}
		} finally {
			if ( cur !=null )
				cur.close();
		}
	}



	public static int getLatestWatchId(Activity activity) {
		Uri uriWatches = Watches.CONTENT_URI;
		String[] columns = new String[] { Watches._ID };
		
		Cursor cur=null;
		try {
			cur = activity.managedQuery(uriWatches, columns, null, null, Logs._ID+" DESC");
			if (cur.moveToFirst()) {
				return cur.getInt(cur.getColumnIndex(Watches._ID));
			} else {
				return -1;
			}
		} finally {
			if ( cur !=null )
				cur.close();
		}
	}

}
