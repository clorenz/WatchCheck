package de.uhrenbastler.watchcheck.data;

import java.util.Calendar;

import android.net.Uri;
import android.provider.BaseColumns;


public class Log {
	
	long id;
	long watchId;
	String modus;
	Calendar localTimestamp;
	double ntpDiff;
	double deviation;
	boolean flagReset;
	String position;
	int temperature;
	String comment;
	double dailyDeviation;
	
    public Log() {}
    
    
    
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
	 * @return the watchId
	 */
	public long getWatchId() {
		return watchId;
	}



	/**
	 * @param watchId the watchId to set
	 */
	public void setWatchId(long watchId) {
		this.watchId = watchId;
	}



	/**
	 * @return the modus
	 */
	public String getModus() {
		return modus;
	}



	/**
	 * @param modus the modus to set
	 */
	public void setModus(String modus) {
		this.modus = modus;
	}



	/**
	 * @return the localTimestamp
	 */
	public Calendar getLocalTimestamp() {
		return localTimestamp;
	}



	/**
	 * @param localTimestamp the localTimestamp to set
	 */
	public void setLocalTimestamp(Calendar localTimestamp) {
		this.localTimestamp = localTimestamp;
	}



	/**
	 * @return the ntpDiff
	 */
	public double getNtpDiff() {
		return ntpDiff;
	}



	/**
	 * @param ntpDiff the ntpDiff to set
	 */
	public void setNtpDiff(double ntpDiff) {
		this.ntpDiff = ntpDiff;
	}



	/**
	 * @return the deviation
	 */
	public double getDeviation() {
		return deviation;
	}



	/**
	 * @param deviation the deviation to set
	 */
	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}



	/**
	 * @return the flagReset
	 */
	public boolean isFlagReset() {
		return flagReset;
	}



	/**
	 * @param flagReset the flagReset to set
	 */
	public void setFlagReset(boolean flagReset) {
		this.flagReset = flagReset;
	}



	/**
	 * @return the position
	 */
	public String getPosition() {
		return position;
	}



	/**
	 * @param position the position to set
	 */
	public void setPosition(String position) {
		this.position = position;
	}



	/**
	 * @return the temperature
	 */
	public int getTemperature() {
		return temperature;
	}



	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(int temperature) {
		this.temperature = temperature;
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
	
	
	

	/**
	 * @return the dailyDeviation
	 */
	public double getDailyDeviation() {
		return dailyDeviation;
	}



	/**
	 * @param dailyDeviation the dailyDeviation to set
	 */
	public void setDailyDeviation(double dailyDeviation) {
		this.dailyDeviation = dailyDeviation;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Log [id=" + id + ", watchId=" + watchId + ", modus=" + modus
				+ ", localTimestamp=" + localTimestamp.getTime() + ", ntpDiff=" + ntpDiff
				+ ", deviation=" + deviation + ", flagReset=" + flagReset
				+ ", position=" + position + ", temperature=" + temperature
				+ ", comment=" + comment + "]";
	}




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
        
        public static final String DEVIATION = "deviation";
        
        /**
         * This flag, when set, indicates the begin of a new measure period
         */
        public static final String FLAG_RESET = "reset";
        
        public static final String POSITION = "position";
        
        public static final String TEMPERATURE = "temperature";
        
        public static final String COMMENT = "comment";
    }




	
}
