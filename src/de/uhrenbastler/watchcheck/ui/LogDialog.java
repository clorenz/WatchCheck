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
package de.uhrenbastler.watchcheck.ui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.ArrayUtils;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.data.Log;
import de.uhrenbastler.watchcheck.data.Log.Logs;
import de.uhrenbastler.watchcheck.tools.Logger;

public class LogDialog extends Dialog {
	
	private static final String[] POSITIONARR = { "","DU","DD","0U","3U","6U","9U" };
	private static final int[] TEMPARR = { -273, 4, 20, 36 };
	boolean saved =false;
	private int watchId;
	private double deviation;
	private boolean modeNtp;
	private GregorianCalendar localTime;
	private GregorianCalendar ntpTime;
	private Spinner positionSpinner;
	private Spinner temperatureSpinner;
	private CheckBox startFlag;
	private EditText comment;
	private Log lastLog;
	
	protected static final String ATTR_DEVIATION = "attrDeviation";
	protected static final String ATTR_WATCH_ID = "attrWatchId";
	protected static final String ATTR_MODE_NTP = "attrModeNtp";
	protected static final String ATTR_LOCAL_TIME = "attrLocalTime";
	protected static final String ATTR_NTP_TIME = "attrNtpTime";
	protected static final String ATTR_LAST_LOG = "lastLog";
	
	public LogDialog(Context context, Bundle logData) {
		super(context);
	
		setContentView(R.layout.log_dialog);
		setTitle(getContext().getString(R.string.enterLog));
		setCancelable(true);
		
		Button buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				makeLogEntry();
				saved=true;
				dismiss();
			}
		});
		
		watchId = logData.getInt(ATTR_WATCH_ID);
		deviation = logData.getDouble(ATTR_DEVIATION);
		modeNtp = logData.getBoolean(ATTR_MODE_NTP);
		localTime = (GregorianCalendar) logData.get(ATTR_LOCAL_TIME);
		ntpTime = (GregorianCalendar) logData.get(ATTR_NTP_TIME);
		lastLog = (Log) logData.get(ATTR_LAST_LOG);
		
		Logger.debug("watchId="+watchId+", deviation="+deviation
			+", modeNtp="+modeNtp+", localTime="+localTime.getTime()
			+", ntpTime="+(ntpTime!=null?ntpTime.getTime():"NULL")
			+", lastLog="+lastLog);
		
		TextView textDeviation = (TextView) findViewById(R.id.textViewDeviationValue);
        DecimalFormat df = new DecimalFormat("#.#");
        
        textDeviation.setText( (deviation>0?"+":deviation<0?"-":"+-") + df.format(Math.abs(deviation)) +" sec." );
        
        positionSpinner = (Spinner) findViewById(R.id.logSpinnerPosition); 
        ArrayAdapter<?> positionAdapter = ArrayAdapter.createFromResource( getContext(),
        		R.array.positions,android.R.layout.simple_spinner_item); 
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(positionAdapter); 
        
        if ( lastLog!=null && lastLog.getPosition()!=null)
        	positionSpinner.setSelection(ArrayUtils.indexOf(POSITIONARR, lastLog.getPosition()));
        else
        	positionSpinner.setSelection(0);
        
        temperatureSpinner = (Spinner) findViewById(R.id.logSpinnerTemperature); 
        ArrayAdapter<?> temperatureAdapter = ArrayAdapter.createFromResource( getContext(),
        		R.array.temperatures,android.R.layout.simple_spinner_item); 
        temperatureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temperatureSpinner.setAdapter(temperatureAdapter); 
        
        if ( lastLog!=null  )
        	temperatureSpinner.setSelection(ArrayUtils.indexOf(TEMPARR, lastLog.getTemperature()));
        else
        	temperatureSpinner.setSelection(0);
        
        comment = (EditText) findViewById(R.id.logComment);
        
        startFlag = (CheckBox) findViewById(R.id.logCheckBoxNewPeriod);
        startFlag.setChecked(lastLog==null);
        startFlag.setEnabled(lastLog!=null);
	}


	/* (non-Javadoc)
	 * @see android.app.Dialog#setOnCancelListener(android.content.DialogInterface.OnCancelListener)
	 */
	@Override
	public void setOnCancelListener(OnCancelListener listener) {
		saved=false;
		dismiss();
	}
	
	
	public boolean isSaved() {
		return saved;
	}
	
	
	/**
	 * Creates the log entry in the content provider
	 */
	protected void makeLogEntry() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		
		double ntpDiff = modeNtp?((double)(localTime.getTimeInMillis() - ntpTime.getTimeInMillis())) / 1000d:0;		// Really???
		
		ContentValues values = new ContentValues();
		values.put(Logs.COMMENT, comment.getEditableText().toString());
		values.put(Logs.DEVIATION, deviation);
		values.put(Logs.FLAG_RESET, startFlag.isChecked());
		values.put(Logs.LOCAL_TIMESTAMP, dateFormat.format(localTime.getTime()));
		values.put(Logs.MODUS, modeNtp);
		values.put(Logs.NTP_DIFF, ntpDiff);
		
		if ( positionSpinner.getSelectedItemId() > 0)
			values.put(Logs.POSITION, POSITIONARR[(int)positionSpinner.getSelectedItemId()]);
		if ( temperatureSpinner.getSelectedItemId() > 0 )
			values.put(Logs.TEMPERATURE, TEMPARR[(int)temperatureSpinner.getSelectedItemId()]);
		values.put(Logs.WATCH_ID, watchId);
		
		
		Logger.debug("values="+values);
				
		getContext().getContentResolver().insert(Logs.CONTENT_URI, values);
		
	}

}
