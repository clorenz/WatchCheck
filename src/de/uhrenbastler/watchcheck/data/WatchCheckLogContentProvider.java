package de.uhrenbastler.watchcheck.data;

import java.util.HashMap;
import java.util.Map;

import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.data.Watch.Watches;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

// -> http://thinkandroid.wordpress.com/2010/01/13/writing-your-own-contentprovider/

public class WatchCheckLogContentProvider extends ContentProvider {
    
    public static final String AUTHORITY = "de.uhrenbastler.watchcheck.data.WatchCheckLogContentProvider";
    private static final UriMatcher sUriMatcher;
    private static final int WATCHES = 1;
    private static final int LOGS = 2;
    private DatabaseHelper dbHelper = new DatabaseHelper(getContext());
    private static Map<String, String> watchesProjectionMap;
    private static Map<String, String> logsProjectionMap;
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "watches", 1);
        sUriMatcher.addURI(AUTHORITY, "logs", 2);
        
        watchesProjectionMap = new HashMap<String,String>();
        watchesProjectionMap.put(Watches.WATCH_ID, Watches.WATCH_ID);
        watchesProjectionMap.put(Watches.NAME, Watches.NAME);
        watchesProjectionMap.put(Watches.SERIAL, Watches.SERIAL);
        watchesProjectionMap.put(Watches.DATE_CREATE, Watches.DATE_CREATE);
        watchesProjectionMap.put(Watches.SERIAL, Watches.SERIAL);
        
        logsProjectionMap = new HashMap<String,String>();
        logsProjectionMap.put(Logs.LOG_ID, Logs.LOG_ID);
        logsProjectionMap.put(Logs.WATCH_ID, Logs.WATCH_ID);
        logsProjectionMap.put(Logs.MODUS, Logs.MODUS);
        logsProjectionMap.put(Logs.LOCAL_TIMESTAMP, Logs.LOCAL_TIMESTAMP);
        logsProjectionMap.put(Logs.NTP_DIFF, Logs.NTP_DIFF);
        logsProjectionMap.put(Logs.POSITION, Logs.POSITION);
        logsProjectionMap.put(Logs.TEMPERATURE, Logs.TEMPERATURE);
        logsProjectionMap.put(Logs.COMMENT, Logs.COMMENT);
        
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                count = db.delete("watches", selection, selectionArgs);
                break;

            case LOGS:
                count = db.delete("logs", selection, selectionArgs);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case WATCHES: 
                return Watches.CONTENT_TYPE;
                
            case LOGS:
                return Logs.CONTENT_TYPE;
                
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                return updateWatches(uri, values);
                
            case LOGS:
                return updateLogs(uri, values);

        }
        
        throw new IllegalArgumentException("Unknown URI " + uri);
    }


    
    private Uri updateWatches(Uri uri, ContentValues initialValues) {
        ContentValues values;
        
        if ( initialValues!=null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        long rowId = db.insert("watches", Watches.NAME, values);
        
        if ( rowId > 0) {
            Uri watchUri = ContentUris.withAppendedId(Watches.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(watchUri, null);
            return watchUri;
        } else {
            throw new SQLException("Could not insert into "+uri);
        }
    }
    
    
    private Uri updateLogs(Uri uri, ContentValues initialValues) {
        ContentValues values;
        
        if ( initialValues!=null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        long rowId = db.insert("Logs", Logs.MODUS, values);
        
        if ( rowId > 0) {
            Uri LogUri = ContentUris.withAppendedId(Logs.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(LogUri, null);
            return LogUri;
        } else {
            throw new SQLException("Could not insert into "+uri);
        }
    }


    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        switch (sUriMatcher.match(uri)) {
            case WATCHES:
                qb.setTables("watches");
                qb.setProjectionMap(watchesProjectionMap);
                break;
                
            case LOGS:
                qb.setTables("logs");
                qb.setProjectionMap(logsProjectionMap);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown URI: "+uri);
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    
    
}
