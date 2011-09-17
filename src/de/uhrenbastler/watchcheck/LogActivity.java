package de.uhrenbastler.watchcheck;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import de.uhrenbastler.watchcheck.data.Log.Logs;


public class LogActivity extends Activity {
	
	private static final String[] POSITIONARR = { "", "DU", "DD", "0U", "3U", "6U", "9U" };
	private static final int[] TEMPARR = { -273, 4, 20, 36 };
	
	
    protected static final String ATTR_DEVIATION = "attrDeviation";
	protected static final String ATTR_WATCH_ID = "attrWatchId";
	protected static final String ATTR_MODE_NTP = "attrModeNtp";
	protected static final String ATTR_LOCAL_TIME = "attrLocalTime";
	protected static final String ATTR_NTP_TIME = "attrNtpTime";
	
	private int watchId;
	private double deviation;
	private boolean modeNtp;
	private GregorianCalendar localTime;
	private GregorianCalendar ntpTime;
	private Spinner positionSpinner;
	private Spinner temperatureSpinner;
	private CheckBox startFlag;
	private EditText comment;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.w("WatchCheck", "LogActivity called without data!!");
			return;
		}
		
		watchId = extras.getInt(ATTR_WATCH_ID);
		deviation = extras.getDouble(ATTR_DEVIATION);
		modeNtp = extras.getBoolean(ATTR_MODE_NTP);
		localTime = (GregorianCalendar) extras.get(ATTR_LOCAL_TIME);
		ntpTime = (GregorianCalendar) extras.get(ATTR_NTP_TIME);
		
		Log.d("WatchCheck","watchId="+watchId+", deviation="+deviation
			+", modeNtp="+modeNtp+", localTime="+localTime.getTime()
			+", ntpTime="+ntpTime.getTime());
        
        setContentView(R.layout.log); 
        
        TextView textDeviation = (TextView) findViewById(R.id.textViewDeviationValue);
        DecimalFormat df = new DecimalFormat("#.#");
        
        textDeviation.setText( (deviation>0?"+":deviation<0?"-":"+-") + df.format(Math.abs(deviation)) +" sec." );
        
        positionSpinner = (Spinner) findViewById(R.id.logSpinnerPosition); 
        ArrayAdapter<?> positionAdapter = ArrayAdapter.createFromResource( this,
        		R.array.positions,android.R.layout.simple_spinner_item); 
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(positionAdapter); 
        
        temperatureSpinner = (Spinner) findViewById(R.id.logSpinnerTemperature); 
        ArrayAdapter<?> temperatureAdapter = ArrayAdapter.createFromResource( this,
        		R.array.temperatures,android.R.layout.simple_spinner_item); 
        temperatureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temperatureSpinner.setAdapter(temperatureAdapter); 
        
        comment = (EditText) findViewById(R.id.logComment);
        
        startFlag = (CheckBox) findViewById(R.id.logCheckBoxNewPeriod);
        
        // OK button actually logs, displays an "OK" dialog, and after the dialog is acknowledged, closes
        // the activity
        
        
        // TODO: Save-Dioalog
        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	makeLogEntry();
                LogActivity.this.finish();       
            }
        });
        
    }

	/**
	 * Creates the log entry in the content provider
	 */
	protected void makeLogEntry() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		
		ContentValues values = new ContentValues();
		values.put(Logs.COMMENT, comment.getEditableText().toString());
		values.put(Logs.DEVIATION, deviation);
		values.put(Logs.FLAG_RESET, startFlag.isChecked());
		values.put(Logs.LOCAL_TIMESTAMP, dateFormat.format(localTime.getTime()));
		values.put(Logs.MODUS, modeNtp);
		values.put(Logs.NTP_DIFF, (ntpTime.getTimeInMillis() - localTime.getTimeInMillis()) / 1000 );
		values.put(Logs.POSITION, POSITIONARR[(int)positionSpinner.getSelectedItemId()]);
		values.put(Logs.TEMPERATURE, TEMPARR[(int)temperatureSpinner.getSelectedItemId()]);
		values.put(Logs.WATCH_ID, watchId);
		
		
		Log.d("WatchCheck","values="+values);
		
		Uri uri = getContentResolver().insert(Logs.CONTENT_URI, values);
		
		Log.d("WatchCheck","url="+uri);
		
	}
}
