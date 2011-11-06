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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import de.uhrenbastler.watchcheck.NoNetworkException;
import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.data.Log;
import de.uhrenbastler.watchcheck.db.WatchCheckDBHelper;
import de.uhrenbastler.watchcheck.ntp.NtpMessage;
import de.uhrenbastler.watchcheck.tools.Logger;

public class CheckWatchActivity extends Activity {
	
	Button checkButton;
	TimePicker watchtimePicker;
	double ntpDelta=0;
	boolean modeNtp=false;
	AsyncTask<Context, Integer, Integer> updateDeviation;
	int selectedWatchId = -1;
	double deviation=0;
	Log lastLog;
	
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
        	Logger.error(e.getMessage(),e);
        }
		
		checkButton = (Button) findViewById(R.id.buttonCheckTime);
		/*
		checkButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	updateDeviation.cancel(true);
            	final LogDialog logDialog = new LogDialog(v.getContext());
            	logDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if ( logDialog.isSaved()) {
							MainActivity mainActivity = (MainActivity) getParent();
							mainActivity.displayResultTab();
						}
					}
            	});
            	logDialog.show();
            }	
        });
        */
		
		checkButton.setOnTouchListener(new OnTouchListener() {			
	       	 @Override
	       	 public boolean onTouch(View v, MotionEvent event) {
	       		 if (event.getAction() == MotionEvent.ACTION_DOWN) {
	       			 Bundle logData = calcLogData();
	       			 
	       			 updateDeviation.cancel(true);
	       			 final LogDialog logDialog = new LogDialog(v.getContext(), logData);
	       			 logDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (logDialog.isSaved()) {
								MainActivity mainActivity = (MainActivity) getParent();
								mainActivity.displayResultTab();
							} else {
								updateDeviation = new UpdateDeviation();
						        updateDeviation.execute(CheckWatchActivity.this);
							}
						}
	            	});
	            	 logDialog.show();
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
		((MainActivity)getParent()).releaseKeepScreenOn();
		super.onPause();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		CurrentActivity.getInstance().setCurrent("CheckWatchActivity");
		
		if ( getParent() instanceof MainActivity) {
			MainActivity mainActivity = (MainActivity) getParent();
			mainActivity.refreshTabs();
		}
        
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		selectedWatchId = preferences.getInt(MainActivity.PREFERENCE_CURRENT_WATCH, -1);
        
        TextView watchToCheck = (TextView) findViewById(R.id.watchModel);
        
        try {
        	watchToCheck.setText(WatchCheckDBHelper.getWatchFromDatabase(selectedWatchId, getContentResolver()).getAsTitleString());
        } catch ( Exception e) {
        	Logger.warn("Want to access watch "+selectedWatchId+", which no longer exists in the database!");
        	selectedWatchId=-1;
        }
        	
        GregorianCalendar now = new GregorianCalendar();
        now.add(Calendar.MINUTE, 1);
        
        lastLog = WatchCheckDBHelper.getLastLogOfWatch(this,selectedWatchId);
        if ( lastLog != null)
        	now.add(Calendar.SECOND, (int)lastLog.getDeviation());
        
        watchtimePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        watchtimePicker.setCurrentMinute(now.get(Calendar.MINUTE));  
        watchtimePicker.setBackgroundColor(getResources().getColor(modeNtp?R.color.light_green:R.color.light_yellow));
        
        updateDeviation = new UpdateDeviation();
        updateDeviation.execute(this);
        
        ((MainActivity)getParent()).setKeepScreenOn();
    }
	
	
	private Bundle calcLogData() {
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
        
        Bundle logData = new Bundle();
        logData.putInt(LogDialog.ATTR_WATCH_ID, selectedWatchId);
        logData.putDouble(LogDialog.ATTR_DEVIATION, deviation);			// relative to system time
        logData.putBoolean(LogDialog.ATTR_MODE_NTP, modeNtp);
        logData.putSerializable(LogDialog.ATTR_LOCAL_TIME, localTime);		// Handy-Zeit
        logData.putSerializable(LogDialog.ATTR_NTP_TIME, modeNtp?referenceTime:null);
        logData.putSerializable(LogDialog.ATTR_LAST_LOG, lastLog);
        
        Logger.debug("modeNtp="+modeNtp);
        
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
		return logData;
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
            Logger.info("NTP request sent, waiting for response...\n");
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
            Logger.info(msg.toString());
            
            Logger.info("Dest. timestamp:     " +
                NtpMessage.timestampToString(destinationTimestamp));
            
            Logger.info("Round-trip delay: " +
                new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
            
            Logger.info("Local clock offset: " +
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
			Logger.info("Killed live deviation display");
		}



		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			runnable=false;
			Logger.info("Killed live deviation display");
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
