package de.uhrenbastler.watchcheck.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class Watch {
    
	long id;
	String name;
	String serial;
	String comment;
	
    public Watch() {}
    
    public Watch(long id, String name, String serial, String comment) {
		this.id = id;
		this.name = name;
		this.serial = serial;
		this.comment = comment;
	}
    
    

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the serial
	 */
	public String getSerial() {
		return serial;
	}

	/**
	 * @param serial the serial to set
	 */
	public void setSerial(String serial) {
		this.serial = serial;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	

	public CharSequence getAsTitleString() {
		StringBuffer ret = new StringBuffer();
		ret.append(name);
		if ( serial!=null && serial.length()>0) {
			ret.append(" (");
			ret.append(serial);
			ret.append(")");
		}
		
		return ret.toString();
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Watch [id=" + id + ", name=" + name + ", serial=" + serial
				+ ", comment=" + comment + "]";
	}






	public static final class Watches implements BaseColumns {
        
        private Watches() {}
        
        public static final String TABLE_NAME="watches";
        
        public static final Uri CONTENT_URI = Uri.parse("content://"+WatchCheckLogContentProvider.AUTHORITY+"/watches");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.watchcheck.watches";
        
        public static final String WATCH_ID = "_id";
        
        public static final String NAME = "name";
        
        public static final String SERIAL = "serial";
        
        public static final String DATE_CREATE = "date_create";
        
        public static final String COMMENT = "comment";

		public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME+" (" +
                    WATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME + " VARCHAR(255), "+
                    SERIAL + " VARCHAR(255), "+
                    DATE_CREATE + " TIMESTAMP, "+
                    COMMENT+" TEXT);";
    }



}
