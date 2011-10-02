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
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Watch.Watches;

public class Exporter {
	
	Activity act;
	
	public String export(Activity act) throws ExportException, FileNotFoundException {
		this.act = act;
		String filename = Environment.getExternalStorageDirectory()+"/watchcheck.data";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename)));
		
		try {
			exportWatches(writer);
			writer.write("------------------------------\n");
			exportLogs(writer);
		} catch ( Exception e ) {
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
	
	
	public void exportWatches(BufferedWriter writer) throws ExportException, FileNotFoundException {
		Uri uriWatches = Watches.CONTENT_URI;
		String[] columns = new String[] { Watches._ID, 
										  Watches.NAME,
										  Watches.SERIAL, 
										  Watches.DATE_CREATE,
										  Watches.COMMENT, };
		
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
		}
	}
	
	
	
	public void exportLogs(BufferedWriter writer) throws ExportException, FileNotFoundException {
		Uri uriLogs = Logs.CONTENT_URI;
		String[] columns = new String[] { Logs._ID, 
										  Logs.WATCH_ID,
										  Logs.MODUS, 
										  Logs.LOCAL_TIMESTAMP,
										  Logs.NTP_DIFF, 
										  Logs.DEVIATION,
										  Logs.FLAG_RESET,
										  Logs.POSITION,
										  Logs.TEMPERATURE,
										  Logs.COMMENT};
		try {
			Cursor cur=null;
			try {
				cur = act.managedQuery(uriLogs, columns, null, null, Logs._ID+" asc");
				if (cur.moveToFirst()) {
					Long id = null;
					Long watchId = null;
					String modus = null;
					String localTimestamp = null;
					Double ntpDiff = null;
					Double deviation = null;
					String flagReset = null;
					String position = null;
					Integer temperature = null;
					String comment = null;
					do {
						id = cur.getLong(cur.getColumnIndex(Logs._ID));
						watchId = cur.getLong(cur.getColumnIndex(Logs.WATCH_ID));
						modus = cur.getString(cur.getColumnIndex(Logs.MODUS));
						localTimestamp = cur.getString(cur.getColumnIndex(Logs.LOCAL_TIMESTAMP));
						ntpDiff = cur.getDouble(cur.getColumnIndex(Logs.NTP_DIFF));
						deviation = cur.getDouble(cur.getColumnIndex(Logs.DEVIATION));
						flagReset = cur.getString(cur.getColumnIndex(Logs.FLAG_RESET));
						position = cur.getString(cur.getColumnIndex(Logs.POSITION));
						temperature = cur.getInt(cur.getColumnIndex(Logs.TEMPERATURE));
						comment = cur.getString(cur.getColumnIndex(Logs.COMMENT));
						
						writer.write(id+"|"+watchId+"|"+modus+"|"+localTimestamp+
								"|"+ntpDiff+"|"+deviation+"|"+flagReset+
								"|"+position+"|"+temperature+"|"+comment+"\n");
						writer.flush();
					} while (cur.moveToNext());
				}
			} finally {
				if ( cur !=null )
					cur.close();
			}	
		} catch ( IOException e) {
			android.util.Log.e("WatchCheck",e.getMessage());
		}
	}

}
