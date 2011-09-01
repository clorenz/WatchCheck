package de.uhrenbastler.watchcheck.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class Watch {
    
    public Watch() {}
    
    public static final class Watches implements BaseColumns {
        
        private Watches() {}
        
        public static final Uri CONTENT_URI = Uri.parse("content://"+WatchCheckLogContentProvider.AUTHORITY+"/watches");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.watchcheck.watches";
        
        public static final String WATCH_ID = "_id";
        
        public static final String NAME = "name";
        
        public static final String SERIAL = "serial";
        
        public static final String DATE_CREATE = "date_create";
        
        public static final String COMMENT = "comment";
    }

}
