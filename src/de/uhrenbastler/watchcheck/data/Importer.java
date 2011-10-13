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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Watch.Watches;


// BIG FAT PROBLEM: Deleting database provides no way to afterwards re-create the database!!!


public class Importer {
	
	Activity act;
	
	public void doImport(Activity act) throws FileNotFoundException {
		this.act = act;
		
		String filename = Environment.getExternalStorageDirectory()+"/watchcheck.data";
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		
		List<String> watchesData=new ArrayList<String>();
		List<String> logsData=new ArrayList<String>();
		boolean readLogs=false;
		
		try {
			String line;
			while ( (line = reader.readLine()) != null) {
				if ( line.startsWith("----------------"))
					readLogs=true;
				else {
					if ( readLogs )
						logsData.add(line);
					else
						watchesData.add(line);
				}
			}
		} catch ( Exception e) {
			android.util.Log.e("WatchCheck","Could not import data: "+e.getMessage());
			return;
		} finally {
			if ( reader!=null)
				try {
					reader.close();
				} catch (Exception e) {
					android.util.Log.w("WatchCheck","Could not close file: "+e.getMessage());
				}
		}
		
		android.util.Log.i("WatchCheck","CR="+act.getContentResolver().getClass().getName());
		
		act.managedQuery(Uri.parse("content://"+WatchCheckLogContentProvider.AUTHORITY+"/close"), null, null, null, null);
		
		if ( act.deleteDatabase(WatchCheckLogContentProvider.WATCHCHECK_DB_NAME))			
			android.util.Log.i("WatchCheck","Deleted database "+WatchCheckLogContentProvider.WATCHCHECK_DB_NAME);
		else
			android.util.Log.e("WatchCheck","Could not delete "+WatchCheckLogContentProvider.WATCHCHECK_DB_NAME);
		
		SQLiteDatabase sqlDb = act.openOrCreateDatabase(WatchCheckLogContentProvider.WATCHCHECK_DB_NAME, 
				Context.MODE_WORLD_WRITEABLE, null);

		sqlDb.setVersion(WatchCheckLogContentProvider.DB_VERSION);
		
		importWatches(watchesData, sqlDb);
		android.util.Log.i("WatchCheck","Imported watches");
		importLogs(logsData, sqlDb);
		android.util.Log.i("WatchCheck","Imported logs. DONE with import!");
		sqlDb.close();
	}

	
	private void importWatches(List<String> watchesData, SQLiteDatabase sqlDb) {
		
		sqlDb.execSQL(Watches.CREATE_TABLE_STATEMENT);
		
		for ( String watchData: watchesData) {
			String[] watchDataParts = watchData.split("\\|");
			
			ContentValues values = new ContentValues();
			values.put(Watches._ID, watchDataParts[0]);			
			values.put(Watches.NAME, watchDataParts[1]);
			values.put(Watches.SERIAL, watchDataParts[2]);
			if ( !"null".equals(watchDataParts[3]))
				values.put(Watches.DATE_CREATE, watchDataParts[3]);
			if ( watchDataParts.length == 5)
				values.put(Watches.COMMENT, watchDataParts[4]);
			
			android.util.Log.d("WatchCheck", values.toString());
			sqlDb.insert(Watches.TABLE_NAME, null, values);
		}
		
	}
	

	private void importLogs(List<String> logsData, SQLiteDatabase sqlDb) {
		
		sqlDb.execSQL(Logs.CREATE_TABLE_STATEMENT);
		
		for ( String logData: logsData) {
			String[] logDataParts = logData.split("\\|");
			
			ContentValues values = new ContentValues();
			values.put(Logs._ID, logDataParts[0]);
			values.put(Logs.WATCH_ID, logDataParts[1]);
			values.put(Logs.MODUS, logDataParts[2]);
			values.put(Logs.LOCAL_TIMESTAMP, logDataParts[3]);
			values.put(Logs.NTP_DIFF, logDataParts[4]);
			values.put(Logs.DEVIATION, logDataParts[5]);
			values.put(Logs.FLAG_RESET, logDataParts[6]);
			if ( !"null".equals(logDataParts[7]))
				values.put(Logs.POSITION, logDataParts[7]);
			values.put(Logs.TEMPERATURE, logDataParts[8]);
			if ( logDataParts.length == 10)
				values.put(Logs.COMMENT, logDataParts[9]);
			
			android.util.Log.d("WatchCheck", values.toString());
			sqlDb.insert(Logs.TABLE_NAME, null, values);
		}
		
	}


}
