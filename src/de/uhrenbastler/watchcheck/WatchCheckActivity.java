package de.uhrenbastler.watchcheck;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.ntp.NtpMessage;

/**
 * In dieser Activity wird der Stand der Uhr ermittelt
 * @author clorenz
 * @created on 10.09.2011
 */
public class WatchCheckActivity extends Activity  {
    
    private Button checkButton;
    private TimePicker watchtimePicker;
    private double ntpDelta=0;
    private double deviation=0;
    private int selectedWatchId = -1;
    private boolean modeNtp=false;
    private AsyncTask<Context, Integer, Integer> updateDeviation;
    
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.watchcheck);
        
        watchtimePicker = (TimePicker) findViewById(R.id.TimePicker1);
        watchtimePicker.setIs24HourView(true);
                
        try {
            ntpDelta = getNtpDelta();
            
            DecimalFormat df = new DecimalFormat("#.##");
            String deviationString = df.format(Math.abs(ntpDelta));
            if ( ntpDelta<0)
            	deviationString = "+"+deviationString;
            else
            	deviationString = "-"+deviationString;
            getParent().setTitle(getResources().getString(R.string.app_name) + " [NTP; deviation="+deviationString+"sec]");
            
            modeNtp=true;
            
        } catch ( Exception e) {
            Log.e("WatchCheck", e.getMessage());
        }
        
        checkButton = (Button) findViewById(R.id.buttonCheckTime);
        this.checkButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent logIntent = measureAndGenerateIntent();              
                startActivity(logIntent);
            }	
        });
        
        this.checkButton.setOnTouchListener(new OnTouchListener() {			
        	 @Override
        	 public boolean onTouch(View v, MotionEvent event) {
        		 if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			 Intent logIntent = measureAndGenerateIntent();              
                     startActivity(logIntent);
        		 }
        		 return false;
        	 }
		});
    }
    
    
    
    
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		updateDeviation.cancel(true);
		super.onPause();
	}


	


	private Intent measureAndGenerateIntent() {
		GregorianCalendar referenceTime = new GregorianCalendar();			// NTP-Zeit
                        
        GregorianCalendar localTime = new GregorianCalendar();
        localTime.setTimeInMillis(referenceTime.getTimeInMillis());			// Handy-Zeit
        
        
        referenceTime.add(Calendar.MILLISECOND, (int)(1000 * ntpDelta));
       
        Integer minute = watchtimePicker.getCurrentMinute();
        Integer hour = watchtimePicker.getCurrentHour();
        
        GregorianCalendar watchTime = new GregorianCalendar();
        watchTime.set(Calendar.HOUR_OF_DAY, hour);
        watchTime.set(Calendar.MINUTE, minute);
        watchTime.set(Calendar.SECOND,0);
        watchTime.set(Calendar.MILLISECOND,0);
        
        // Precision: 1/10 sec
        deviation = (float)(watchTime.getTimeInMillis() - localTime.getTimeInMillis()) / 1000;
        
        Intent logIntent = new Intent(WatchCheckActivity.this, LogActivity.class);
        logIntent.putExtra(LogActivity.ATTR_DEVIATION, deviation);			// relative to system time
        logIntent.putExtra(LogActivity.ATTR_WATCH_ID, selectedWatchId);
        logIntent.putExtra(LogActivity.ATTR_MODE_NTP, modeNtp);
        logIntent.putExtra(LogActivity.ATTR_LOCAL_TIME, localTime);		// Handy-Zeit
        logIntent.putExtra(LogActivity.ATTR_NTP_TIME, modeNtp?referenceTime:null);
        
        Log.d("WatchCheck", "modeNtp="+modeNtp);
        
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
		return logIntent;
	}
    
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
        
        TextView watchToCheck = (TextView) findViewById(R.id.watchModel);
        
        try {
        	watchToCheck.setText(WatchCheckDBHelper.getWatchFromDatabase(selectedWatchId, getContentResolver()).getAsTitleString());
        } catch ( Exception e) {
        	Log.w("WatchCheck","Want to access watch "+selectedWatchId+", which no longer exists in the database!");
        	selectedWatchId=-1;
        }
        	
        GregorianCalendar now = new GregorianCalendar();
        now.add(Calendar.MINUTE, 1);
        
        watchtimePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        watchtimePicker.setCurrentMinute(now.get(Calendar.MINUTE));
        
        updateDeviation = new UpdateDeviation();
        updateDeviation.execute(this);
    }

		

    protected double getNtpDelta() throws IOException, NoNetworkException {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        
        if ( networkInfo!=null && networkInfo.isConnected()) {       
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(2000);
            InetAddress address = InetAddress.getByName("europe.pool.ntp.org");
            byte[] buf = new NtpMessage().toByteArray();
            DatagramPacket packet =
                new DatagramPacket(buf, buf.length, address, 123);
            
            // Set the transmit timestamp *just* before sending the packet
            // ToDo: Does this actually improve performance or not?
            NtpMessage.encodeTimestamp(packet.getData(), 40,
                (System.currentTimeMillis()/1000.0) + 2208988800.0);
            
            socket.send(packet);
            
            
            // Get response
            Log.i("WatchCheck","NTP request sent, waiting for response...\n");
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            
            // Immediately record the incoming timestamp
            double destinationTimestamp =
                (System.currentTimeMillis()/1000.0) + 2208988800.0;
            
            
            // Process response
            NtpMessage msg = new NtpMessage(packet.getData());
            
            // Corrected, according to RFC2030 errata
            double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) -
                (msg.transmitTimestamp-msg.receiveTimestamp);
                
            double localClockOffset =
                ((msg.receiveTimestamp - msg.originateTimestamp) +
                (msg.transmitTimestamp - destinationTimestamp)) / 2;
            
            
            // Display response
            Log.i("WatchCheck",msg.toString());
            
            Log.i("WatchCheck","Dest. timestamp:     " +
                NtpMessage.timestampToString(destinationTimestamp));
            
            Log.i("WatchCheck","Round-trip delay: " +
                new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
            
            Log.i("WatchCheck","Local clock offset: " +
                new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
            
            socket.close();
    
            return localClockOffset;
            
        } else {
            throw new NoNetworkException("No network connection. Network info="+networkInfo);
        }
    }
    
    
    /**
     * This inner class is responsible for live display of the current deviation
     * @author clorenz
     * @created on 24.09.2011
     */
    private class UpdateDeviation extends AsyncTask<Context, Integer, Integer> {
    	
    	boolean runnable=true;
    	TextView currentDeviation = (TextView) findViewById(R.id.currentDeviation);
    	double deviation=0;


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Integer result) {
			runnable=false;
			Log.d("WatchCheck","Killed live deviation display");
		}



		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			runnable=false;
			Log.d("WatchCheck","Killed live deviation display");
		}



		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			String pre="+";
			if ( deviation < 0 )
				pre="";
	        currentDeviation.setText(pre+new DecimalFormat("0.0").format(deviation)+" sec.");	        
		}



		@Override
		protected Integer doInBackground(Context... params) {
				
			int progress=0;
			
			while ( runnable ) {
				GregorianCalendar referenceTime = new GregorianCalendar();			// NTP-Zeit
	            
		        GregorianCalendar localTime = new GregorianCalendar();
		        localTime.setTimeInMillis(referenceTime.getTimeInMillis());			// Handy-Zeit
		        
		        
		        referenceTime.add(Calendar.MILLISECOND, (int)(1000 * ntpDelta));
		       
		        Integer minute = watchtimePicker.getCurrentMinute();
		        Integer hour = watchtimePicker.getCurrentHour();
		        
		        GregorianCalendar watchTime = new GregorianCalendar();
		        watchTime.set(Calendar.HOUR_OF_DAY, hour);
		        watchTime.set(Calendar.MINUTE, minute);
		        watchTime.set(Calendar.SECOND,0);
		        watchTime.set(Calendar.MILLISECOND,0);
		        
		        // Precision: 1/10 sec
		        deviation = (float)(watchTime.getTimeInMillis() - localTime.getTimeInMillis()) / 1000;
		        
		        publishProgress((++progress % 2));
		        
		        try {
					Thread.sleep(333);
				} catch (InterruptedException ignore) {
				}
			}
			
			return null;
		}
	};
}