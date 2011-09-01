package de.uhrenbastler.watchcheck.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Watch.Watches;


public class DatabaseHelper extends SQLiteOpenHelper {
    
    DatabaseHelper(Context context) {
        super(context, "watchcheck.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE watch (" +
                Watches.WATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                Watches.NAME + " VARCHAR(255), "+
                Watches.SERIAL + " VARCHAR(255), "+
                Watches.DATE_CREATE + " TIMESTAMP, "+
                Watches.COMMENT+" TEXT);");      
        
        db.execSQL("CREATE TABLE logs (" +
                Logs.LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                Logs.WATCH_ID + " INTEGER, "+
                Logs.MODUS + " VARCHAR(5), "+
                Logs.LOCAL_TIMESTAMP + " TIMESTAMP, "+
                Logs.NTP_DIFF + " DECIMAL(6,2), "+
                Logs.POSITION + " VARCHAR(2), "+
                Logs.TEMPERATURE + " INTEGER, "+
                Logs.COMMENT + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalArgumentException("Upgrading the database from "+oldVersion+" to "+newVersion+" is not yet implemented!");
    }
}

