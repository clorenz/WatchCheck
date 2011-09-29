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
package de.uhrenbastler.watchcheck.data;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import de.uhrenbastler.watchcheck.data.Watch.Watches;

public class Exporter {
	
	// TODO: Wrap exportWatches and exportLogs in one single export() method!
	
	public String exportWatches(Activity act) throws ExportException, FileNotFoundException {
		Uri uriWatches = Watches.CONTENT_URI;
		String[] columns = new String[] { Watches._ID, 
										  Watches.NAME,
										  Watches.SERIAL, 
										  Watches.DATE_CREATE,
										  Watches.COMMENT, };
		
		String filename = Environment.getExternalStorageDirectory()+"/watchcheck.data";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename)));
		try {
		Cursor cur=null;
		try {
			cur = act.managedQuery(uriWatches, columns, null, null, Watches._ID+" asc");
			if (cur.moveToFirst()) {
				Long id = null;
				String name = null;
				String serial = null;
				String dateCreate = null;
				String comment = null;
				do {
					id = cur.getLong(cur.getColumnIndex(Watches._ID));
					name = cur.getString(cur.getColumnIndex(Watches.NAME));
					serial = cur.getString(cur.getColumnIndex(Watches.SERIAL));
					dateCreate = cur.getString(cur.getColumnIndex(Watches.DATE_CREATE));
					comment = cur.getString(cur.getColumnIndex(Watches.COMMENT));
					
					writer.write(id+"|"+name+"|"+serial+"|"+dateCreate+"|"+comment+"\n");
					writer.flush();
				} while (cur.moveToNext());
			}
		} finally {
			if ( cur !=null )
				cur.close();
		}	
		} catch ( IOException e) {
			android.util.Log.e("WatchCheck",e.getMessage());
		} finally {
			if ( writer!=null )
				try {
					writer.close();
				} catch (IOException e) {
					android.util.Log.e("WatchCheck",e.getMessage());
				}
		}
		
		return filename;
	}

}
