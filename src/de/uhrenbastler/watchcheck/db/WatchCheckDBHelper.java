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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import de.uhrenbastler.watchcheck.data.Watch;
import de.uhrenbastler.watchcheck.data.Watch.Watches;

public class WatchCheckDBHelper {
	
	public static Watch getWatchFromDatabase(long id, ContentResolver cr) {
    	Uri selectedWatchUri = Uri.withAppendedPath(Watches.CONTENT_URI, ""+id);
    	
    	Log.d("WatchCheck", "Retrieving watch "+id+" from content provider with uri="+selectedWatchUri);
    	
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

}
