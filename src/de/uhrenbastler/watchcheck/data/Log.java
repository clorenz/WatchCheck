package de.uhrenbastler.watchcheck.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class Log {
    
    public Log() {}
    
    public static final class Logs implements BaseColumns {
        
        private Logs() {}
        
        public static final String TABLE_NAME="logs";
        
        public static final Uri CONTENT_URI = Uri.parse("content://"+WatchCheckLogContentProvider.AUTHORITY+"/logs");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.watchcheck.logs";
        
        public static final String LOG_ID = "_id";
        
        public static final String WATCH_ID = "watch_id";
        
        public static final String MODUS = "modus";
        
        public static final String LOCAL_TIMESTAMP = "local_timestamp";             // local timestamp
        
        public static final String NTP_DIFF = "ntpDiff";                // diff local to ntp
        
        /**
         * This flag, when set, indicates the begin of a new measure period
         */
        public static final String FLAG_RESET = "reset";
        
        public static final String POSITION = "position";
        
        public static final String TEMPERATURE = "temperature";
        
        public static final String COMMENT = "comment";
    }

}
